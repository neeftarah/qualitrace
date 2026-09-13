import {Component, inject, signal} from '@angular/core';
import {CommonModule, formatDate} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Table, TableLazyLoadEvent, TableModule} from 'primeng/table';
import {InputTextModule} from 'primeng/inputtext';
import {ButtonModule} from 'primeng/button';
import {TagModule} from 'primeng/tag';
import {IconFieldModule} from 'primeng/iconfield';
import {InputIconModule} from 'primeng/inputicon';
import {DatePickerModule} from 'primeng/datepicker';
import {SelectModule} from 'primeng/select';
import {BatchService} from '../../services/batch.service';
import {Batch} from '../../models/batch.model';
import {DialogModule} from 'primeng/dialog';
import {TooltipModule} from 'primeng/tooltip';
import {ConfirmDialogModule} from 'primeng/confirmdialog';
import {ConfirmationService} from 'primeng/api';
import {HttpResponse} from '@angular/common/http';
import {Toolbar} from "primeng/toolbar";
import { RouterLink } from '@angular/router';

@Component({
    selector: 'batch-list',
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
        ConfirmDialogModule,
        RouterLink
    ],
    providers: [ConfirmationService],
    templateUrl: './batch-list.component.html'
})
export class BatchListComponent {
    private readonly batchService = inject(BatchService);
    private confirmationService = inject(ConfirmationService);

    batchs = signal<Batch[]>([]);
    totalRecords = signal<number>(0);
    loading = signal<boolean>(true);
    pageSize = signal<number>(10);
    componentOptions = signal<{ label: string; value: string | null }[]>([
        {label: 'Tous les composant', value: null}
    ]);
    supplierOptions = signal<{ label: string; value: string | null }[]>([
        {label: 'Tous les fournisseurs', value: null}
    ]);

    // Critères de recherche
    internalBatchNumber: string | null = null;
    supplierBatchNumber: string | null = null;
    expiryFromDate: string | null = null;
    expiryToDate: string | null = null;
    receptionFromDate: string | null = null;
    receptionToDate: string | null = null;
    validationFromDate: string | null = null;
    validationToDate: string | null = null;
    dateReceptionRange: Date[] | null = null;
    dateExpiryRange: Date[] | null = null;
    selectedAuthor: string | null = null;
    selectedStatus: string | null = null;
    selectedComponent: string | null = null;
    selectedSupplier: string | null = null;

    typesOptions = [
        {label: 'Tous les types', value: null},
        {label: 'Matières premières', value: 'RAW_MATERIAL'},
        {label: 'Composants d\'emballage', value: 'COMPONENT'}
    ];
    statusOptions = [
        {label: 'Tous les statuts', value: null},
        {label: 'Quarantaines', value: 'QUARANTINE'},
        {label: 'Libérés', value: 'RELEASED'},
        {label: 'Rejetés', value: 'REJECTED'},
        {label: 'Utilisés', value: 'USED'},
        {label: 'Détruits', value: 'DESTROYED'}
    ];

    ngOnInit(): void {
        this.loadSuppliers();
        this.loadComponents();
    }

    private loadSuppliers(): void {
        this.batchService.getSuppliers().subscribe({
            next: (suppliers) => {
                const options = [
                    {label: 'Tous les fournisseurs', value: null},
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

    private loadComponents(): void {
        this.batchService.getComponents().subscribe({
            next: (components) => {
                const options = [
                    {label: 'Tous les composants', value: null},
                    ...components.map(c => ({
                        label: `${c.name} (${c.reference})`,
                        value: c.id
                    }))
                ];
                this.componentOptions.set(options);
            },
            error: (err) => console.error('Erreur de chargement des composants', err)
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

        let receptionFromDate: string | undefined = undefined;
        if (this.dateReceptionRange && this.dateReceptionRange[0]) {
            receptionFromDate = formatDate(this.dateReceptionRange[0], 'yyyy-MM-dd', 'en-US');
        }

        let receptionToDate: string | undefined = undefined;
        if (this.dateReceptionRange && this.dateReceptionRange[1]) {
            receptionToDate = formatDate(this.dateReceptionRange[1], 'yyyy-MM-dd', 'en-US');
        }

        let expiryFromDate: string | undefined = undefined;
        if (this.dateExpiryRange && this.dateExpiryRange[0]) {
            expiryFromDate = formatDate(this.dateExpiryRange[0], 'yyyy-MM-dd', 'en-US');
        }

        let expiryToDate: string | undefined = undefined;
        if (this.dateExpiryRange && this.dateExpiryRange[1]) {
            expiryToDate = formatDate(this.dateExpiryRange[1], 'yyyy-MM-dd', 'en-US');
        }

        this.batchService.getBatchs({
            page,
            size,
            sort,
            internalBatchNumber: this.internalBatchNumber || undefined,
            componentId: this.selectedComponent || undefined,
            supplierId: this.selectedSupplier || undefined,
            supplierBatchNumber: this.supplierBatchNumber || undefined,
            receptionFromDate,
            receptionToDate,
            expiryFromDate,
            expiryToDate,
            validatedBy: this.selectedAuthor || undefined,
            status: this.selectedStatus || undefined,

        }).subscribe({
            next: (res) => {
                this.batchs.set(res._embedded?.batches || []);
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
        this.internalBatchNumber = null;
        this.supplierBatchNumber = null;
        this.expiryFromDate = null;
        this.expiryToDate = null;
        this.receptionFromDate = null;
        this.receptionToDate = null;
        this.validationFromDate = null;
        this.validationToDate = null;
        this.dateReceptionRange = null;
        this.dateExpiryRange = null;
        this.selectedAuthor = null;
        this.selectedStatus = null;
        this.selectedSupplier = null;
        this.selectedComponent = null;
        dt.reset();
    }

    getStatusClass(event: string): 'info' | 'success' | 'danger' | 'contrast' | 'secondary' {
        switch (event) {
            case 'QUARANTINE':
                return 'info';
            case 'RELEASED':
            case 'USED':
                return 'success';
            case 'REJECTED':
                return 'danger';
            case 'DESTROYED':
                return 'contrast';
            default:
                return 'secondary';
        }
    }

    getStatusName(type: string): string {
        switch (type) {
            case 'QUARANTINE':
                return 'Quarantaine';
            case 'RELEASED':
                return 'Libéré';
            case 'USED':
                return 'Utilisé';
            case 'REJECTED':
                return 'Rejeté';
            case 'DESTROYED':
                return 'Détruit';
            default:
                return type;
        }
    }

    displayModal = signal<boolean>(false);
    selectedBatch = signal<Batch | null>(null);

    openModal(item: Batch | null): void {
        this.displayModal.set(true);
    }

    getCertificateBatch(item: Batch) {
        this.batchService.getCertificateBatch(item).subscribe({
            next: (response: HttpResponse<Blob>) => {
                const blob = response.body!;
                const contentDisposition = response.headers.get('Content-Disposition');
                let filename = `certificat-${item.id}.pdf`;
                if (contentDisposition) {
                    const match = contentDisposition.match(/filename="?([^"]+)"?/);
                    if (match) filename = match[1];
                }
                const url = window.URL.createObjectURL(blob);
                const link = document.createElement('a');
                link.href = url;
                link.download = filename;
                link.click();
                window.URL.revokeObjectURL(url);
            },
            error: (err) => {
                console.error('Erreur lors de la récupération du certificat', err);
            }
        });
    }
}
