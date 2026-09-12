import { Component, inject, OnInit, signal } from '@angular/core';
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
import { SupplierService } from '../../services/supplier.service';
import { Supplier, SupplierHalResponse } from '../../models/supplier.model';
import { DialogModule } from 'primeng/dialog';
import { JsonPipe } from '@angular/common';
import { TooltipModule } from 'primeng/tooltip';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';

@Component({
    selector: 'supplier-list',
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
    templateUrl: './supplier-list.component.html'
})
export class SupplierListComponent {
    private readonly supplierService = inject(SupplierService);
    private confirmationService = inject(ConfirmationService);

    suppliers = signal<Supplier[]>([]);
    totalRecords = signal<number>(0);
    loading = signal<boolean>(true);
    pageSize = signal<number>(10);

    // Critères de recherche
    searchCode: string | null = null;
    searchName: string | null = null;
    selectedStatus: string | null = null;

    statusOptions = [
        { label: 'Tous les statuts', value: null },
        { label: 'Actifs', value: 'ACTIVE' },
        { label: 'Archivés', value: 'ARCHIVED' }
    ];

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

        this.supplierService.getSuppliers({
            page,
            size,
            sort,
            code: this.searchCode || undefined,
            name: this.searchName || undefined,
            status: this.selectedStatus || undefined
        }).subscribe({
            next: (res) => {
                this.suppliers.set(res._embedded?.suppliers || []);
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
        this.searchCode = null;
        this.searchName = null;
        this.selectedStatus = null;
        dt.reset();
    }

    getStatusClass(event: string): 'success' | 'danger' | 'secondary' {
        switch (event) {
            case 'ACTIVE':
                return 'success';
            case 'ARCHIVED':
                return 'danger';
            default: return 'secondary';
        }
    }
    displayModal = signal<boolean>(false);

    openModal(item: Supplier | null): void {
        this.displayModal.set(true);
    }

    archiveSupplier(item: Supplier) {
        this.confirmationService.confirm({
            header: 'Êtes-vous sûr de vouloir archiver ce fournisseur ?',
            message: 'Tous les composants du fournisseur seront également archivés.',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Confirmer',
            rejectLabel: 'Annuler',
            acceptButtonStyleClass: 'p-button-danger',
            rejectButtonStyleClass: 'p-button-secondary p-button-text',
            accept: () => {
                this.supplierService.archiveSupplier(item).subscribe({
                    next: () => {
                        this.suppliers.update(list =>
                            list.map(s => s.id === item.id ? {...s, status: 'ARCHIVED'} : s)
                        );
                    },
                    error: (err) => {
                        console.error('Erreur lors de l\'archivage du fournisseur', err);
                    }
                });
            }
        });
    }

    activateSupplier(item: Supplier) {
        this.supplierService.activateSupplier(item).subscribe({
            next: () => {
                this.suppliers.update(list =>
                    list.map(s => s.id === item.id ? { ...s, status: 'ACTIVE' } : s)
                );
            },
            error: (err) => {
                console.error('Erreur lors de la réactivation du fournisseur', err);
            }
        });
    }
}
