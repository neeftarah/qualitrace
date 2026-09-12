import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, DatePipe, formatDate } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Table, TableLazyLoadEvent, TableModule } from 'primeng/table';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { DatePickerModule } from 'primeng/datepicker';
import { SelectModule } from 'primeng/select';
import { AuditTrailService } from '../../services/audit-trail.service';
import { AuditTrail } from '../../models/audit-trail.model';
import { DialogModule } from 'primeng/dialog';
import { JsonPipe } from '@angular/common';
import { TooltipModule } from 'primeng/tooltip';

export interface DiffItem {
    key: string;
    oldValue: string;
    newValue: string;
    type: 'MODIFIED' | 'ADDED' | 'REMOVED';
}

@Component({
    selector: 'app-audit-trail-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        TableModule,
        InputTextModule,
        ButtonModule,
        TagModule,
        IconFieldModule,
        InputIconModule,
        DatePickerModule,
        SelectModule,
        DatePipe,
        DialogModule,
        ButtonModule,
        TooltipModule
    ],
    templateUrl: './audit-trail-list.component.html'
})
export class AuditTrailListComponent {
    private readonly auditTrailService = inject(AuditTrailService);

    auditTrails = signal<AuditTrail[]>([]);
    totalRecords = signal<number>(0);
    loading = signal<boolean>(true);
    pageSize = signal<number>(10);
    authorOptions = signal<{ label: string; value: string | null }[]>([
        { label: 'Tous les auteurs', value: null }
    ]);

    // Critères de recherche
    searchTerm = '';
    selectedEvent: string | null = null;
    selectedEntityType: string | null = null;
    selectedEntityId: string | null = null;
    selectedAuthorId: string | null = null;
    dateRange: Date[] | null = null;

    ngOnInit(): void {
        this.loadAuthors();
    }

    private loadAuthors(): void {
        this.auditTrailService.getAuthors().subscribe({
            next: (authors) => {
                const options = [
                    { label: 'Tous les auteurs', value: null },
                    ...authors.map(a => ({
                        label: `${a.firstname} ${a.surname} (${a.login})`,
                        value: a.id // Ou a.login selon ce qu'attend le backend pour author_id
                    }))
                ];
                this.authorOptions.set(options);
            },
            error: (err) => console.error('Erreur chargement auteurs', err)
        });
    }

    // Options pour les listes déroulantes
    eventOptions = [
        { label: 'Tous les événements', value: null },
        { label: 'CREATED', value: 'CREATED' },
        { label: 'VALIDATED', value: 'VALIDATED' },
        { label: 'ACTIVATED', value: 'ACTIVATED' },
        { label: 'CLOSED', value: 'CLOSED' },
        { label: 'UNLOCKED', value: 'UNLOCKED' },
        { label: 'UPDATED', value: 'UPDATED' },
        { label: 'USED', value: 'USED' },
        { label: 'DELETED', value: 'DELETED' },
        { label: 'ARCHIVED', value: 'ARCHIVED' },
        { label: 'REFUSED', value: 'REFUSED' },
        { label: 'DESTROYED', value: 'DESTROYED' },
        { label: 'DRAFTED', value: 'DRAFTED' },
        { label: 'OPENED', value: 'OPENED' },
        { label: 'LOCKED', value: 'LOCKED' }
    ];

    entityTypeOptions = [
        { label: 'Toutes les entités', value: null },
        { label: 'Déviation', value: 'DeviationEntity' },
        { label: 'Lot', value: 'BatchEntity' },
        { label: 'Résultat d\'analyses', value: 'AnalysisResultEntity' },
        { label: 'Gamme de contrôles', value: 'SpecificationEntity' },
        { label: 'Composant', value: 'ComponentEntity' },
        { label: 'Fournisseur', value: 'SupplierEntity' },
        { label: 'Utilisateur', value: 'UserEntity' }
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

        let fromDate: string | undefined = undefined;
        let toDate: string | undefined = undefined;
        if (this.dateRange && this.dateRange[0]) {
            fromDate = formatDate(this.dateRange[0], 'yyyy-MM-dd', 'en-US');
        }
        if (this.dateRange && this.dateRange[1]) {
            toDate = formatDate(this.dateRange[1], 'yyyy-MM-dd', 'en-US');
        }

        this.auditTrailService.getAuditTrails({
            page,
            size,
            sort,
            content: this.searchTerm || undefined,
            event: this.selectedEvent || undefined,
            entity_type: this.selectedEntityType || undefined,
            entity_id: this.selectedEntityId?.trim() || undefined,
            author_id: this.selectedAuthorId?.trim() || undefined,
            fromDate,
            toDate
        }).subscribe({
            next: (res) => {
                this.auditTrails.set(res._embedded?.audit_trails || []);
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
        this.searchTerm = '';
        this.selectedEvent = null;
        this.selectedEntityType = null;
        this.selectedEntityId = null;
        this.selectedAuthorId = null;
        this.dateRange = null;
        dt.reset();
    }

    getStatusClass(event: string): 'success' | 'info' | 'warn' | 'danger' | 'secondary' {
        switch (event) {
            case 'CREATED':
            case 'VALIDATED':
            case 'ACTIVATED':
            case 'CLOSED':
            case 'UNLOCKED':
                return 'success';
            case 'UPDATED':
            case 'USED':
                return 'info';
            case 'DELETED':
            case 'ARCHIVED':
            case 'REFUSED':
            case 'DESTROYED':
            case 'DRAFTED':
            case 'OPENED':
            case 'LOCKED':
                return 'danger';
            default: return 'secondary';
        }
    }

    getEntityTypeName(type: string): string {
        switch (type) {
            case 'DeviationEntity' :
                return 'Déviation';
            case 'BatchEntity' :
                return 'Lot';
            case 'AnalysisResultEntity' :
                return 'Résultat d\'analyses';
            case 'SpecificationEntity' :
                return 'Gamme de contrôles';
            case 'ComponentEntity' :
                return 'Composant';
            case 'SupplierEntity' :
                return 'Fournisseur';
            case 'UserEntity':
                return 'Utilisateur';
            default: return type;
        }
    }

    displayDiffModal = signal<boolean>(false);
    selectedAuditTrail = signal<AuditTrail | null>(null);
    activeDiff = signal<DiffItem[]>([]);

    openDiffModal(item: AuditTrail): void {
        this.selectedAuditTrail.set(item);
        this.activeDiff.set(this.computeDiff(item.previous_data, item.changed_data));
        this.displayDiffModal.set(true);
    }

    private computeDiff(prevRaw: any, nextRaw: any): DiffItem[] {
        const prev = this.parseJson(prevRaw);
        const next = this.parseJson(nextRaw);

        const allKeys = Array.from(new Set([...Object.keys(prev), ...Object.keys(next)]));
        const diffs: DiffItem[] = [];

        for (const key of allKeys) {
            const hasPrev = Object.prototype.hasOwnProperty.call(prev, key);
            const hasNext = Object.prototype.hasOwnProperty.call(next, key);
            const val1 = prev[key];
            const val2 = next[key];

            if (hasPrev && !hasNext) {
                diffs.push({ key, oldValue: this.formatVal(val1), newValue: '', type: 'REMOVED' });
            } else if (!hasPrev && hasNext) {
                diffs.push({ key, oldValue: '', newValue: this.formatVal(val2), type: 'ADDED' });
            } else if (JSON.stringify(val1) !== JSON.stringify(val2)) {
                diffs.push({ key, oldValue: this.formatVal(val1), newValue: this.formatVal(val2), type: 'MODIFIED' });
            }
        }

        return diffs;
    }

    private parseJson(data: any): Record<string, any> {
        if (!data) return {};
        if (typeof data === 'object') return data;
        try {
            return JSON.parse(data);
        } catch {
            return {};
        }
    }

    private formatVal(val: any): string {
        if (val === null || val === undefined) return 'null';
        if (typeof val === 'object') return JSON.stringify(val, null, 2);
        return String(val);
    }
}
