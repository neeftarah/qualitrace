import { Component as AngularComponent, inject, OnInit, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Table, TableLazyLoadEvent, TableModule } from 'primeng/table';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { Toolbar } from 'primeng/toolbar';
import { TagModule } from 'primeng/tag';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { DatePickerModule } from 'primeng/datepicker';
import { SelectModule } from 'primeng/select';
import { ComponentService } from '../../services/component.service';
import { Component, ComponentHalResponse } from '../../models/component.model';
import { DialogModule } from 'primeng/dialog';
import { JsonPipe } from '@angular/common';
import { TooltipModule } from 'primeng/tooltip';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import {AuditTrail} from "@/app/features/audit-trail/models/audit-trail.model";

@AngularComponent({
    selector: 'component-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        TableModule,
        InputTextModule,
        ButtonModule,
        TagModule,
        Toolbar,
        IconFieldModule,
        InputIconModule,
        DatePickerModule,
        SelectModule,
        DialogModule,
        ButtonModule,
        TooltipModule,
        ConfirmDialogModule
    ],
    providers: [ConfirmationService],
    templateUrl: './component-list.component.html'
})
export class ComponentListComponent {
    private readonly componentService = inject(ComponentService);
    private confirmationService = inject(ConfirmationService);

    components = signal<Component[]>([]);
    totalRecords = signal<number>(0);
    loading = signal<boolean>(true);
    pageSize = signal<number>(10);
    supplierOptions = signal<{ label: string; value: string | null }[]>([
        { label: 'Tous les fournisseurs', value: null }
    ]);

    // Critères de recherche
    searchReference: string | null = null;
    searchName: string | null = null;
    selectedType: string | null = null;
    selectedStatus: string | null = null;
    selectedSupplier: string | null = null;

    typesOptions = [
        { label: 'Tous les types', value: null },
        { label: 'Matières premières', value: 'RAW_MATERIAL' },
        { label: 'Composants d\'emballage', value: 'COMPONENT' }
    ];
    statusOptions = [
        { label: 'Tous les statuts', value: null },
        { label: 'Quarantaine', value: 'DRAFT' },
        { label: 'Disponibles', value: 'ACTIVE' },
        { label: 'Archivés', value: 'ARCHIVED' }
    ];

    ngOnInit(): void {
        this.loadSuppliers();
    }

    private loadSuppliers(): void {
        this.componentService.getSuppliers().subscribe({
            next: (suppliers) => {
                const options = [
                    { label: 'Tous les fournisseurs', value: null },
                    ...suppliers.map(s => ({
                        label: `${s.name} (${s.code})`,
                        value: s.id
                    }))
                ];
                this.supplierOptions.set(options);
            },
            error: (err) => console.error('Erreur de chargement des fournisseurs', err)
        });
    }

    loadData(event: TableLazyLoadEvent): void {
        this.loading.set(true);

        const page = (event.first ?? 0) / (event.rows ?? 10);
        const size = event.rows ?? 10;

        let sort: string | undefined = undefined;
        if (event.sortField) {
            const field = Array.isArray(event.sortField) ? event.sortField[0] : event.sortField;
            const order = event.sortOrder === 1 ? 'asc' : 'desc';
            sort = `${field},${order}`;
        }

        this.componentService.getComponents({
            page,
            size,
            sort,
            type: this.selectedType || undefined,
            reference: this.searchReference || undefined,
            name: this.searchName || undefined,
            status: this.selectedStatus || undefined,
            supplierId: this.selectedSupplier || undefined
        }).subscribe({
            next: (res) => {
                this.components.set(res._embedded?.components || []);
                this.totalRecords.set(res.page?.totalElements || 0);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }

    applyFilters(dt: Table): void {
        dt.reset();
    }

    resetFilters(dt: Table): void {
        this.selectedType = null;
        this.searchReference = null;
        this.searchName = null;
        this.selectedStatus = null;
        this.selectedSupplier = null;
        dt.reset();
    }

    getStatusClass(event: string): 'info' | 'success' | 'danger' | 'secondary' {
        switch (event) {
            case 'ACTIVE':
                return 'success';
            case 'ARCHIVED':
                return 'danger';
            case 'DRAFT':
                return 'info';
            default: return 'secondary';
        }
    }

    getStatusName(type: string): string {
        switch (type) {
            case 'DRAFT' :
                return 'Quarantaine';
            case 'ACTIVE' :
                return 'Disponible';
            case 'ARCHIVED' :
                return 'Archivé';
            default: return type;
        }
    }

    getTypeName(type: string): string {
        switch (type) {
            case 'RAW_MATERIAL' :
                return 'Matières premières';
            case 'COMPONENT' :
                return 'Composants d\'emballage';
            default: return type;
        }
    }

    displayModal = signal<boolean>(false);
    selectedComponent = signal<Component | null>(null);

    openModal(item: Component | null): void {
        this.displayModal.set(true);
    }

    archiveComponent(item: Component) {
        this.confirmationService.confirm({
            header: 'Êtes-vous sûr de vouloir archiver ce composant ?',
            message: 'Le composant ne sera plus disponible pour la création de nouveaux lots',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Confirmer',
            rejectLabel: 'Annuler',
            acceptButtonStyleClass: 'p-button-danger',
            rejectButtonStyleClass: 'p-button-secondary p-button-text',
            accept: () => {
                this.componentService.archiveComponent(item).subscribe({
                    next: () => {
                        this.components.update(list =>
                            list.map(s => s.id === item.id ? {...s, status: 'ARCHIVED'} : s)
                        );
                    },
                    error: (err) => {
                        console.error('Erreur lors de l\'archivage du composant', err);
                    }
                });
            }
        });
    }

    activateComponent(item: Component) {
        this.componentService.activateComponent(item).subscribe({
            next: () => {
                this.components.update(list =>
                    list.map(s => s.id === item.id ? { ...s, status: (item.status == 'ARCHIVED') ? 'DRAFT' : 'ACTIVE' } : s)
                );
            },
            error: (err) => {
                console.error('Erreur lors de la réactivation du composant', err);
            }
        });
    }
}
