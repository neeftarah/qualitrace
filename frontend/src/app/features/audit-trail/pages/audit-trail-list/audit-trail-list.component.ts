import { Component, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TableLazyLoadEvent, TableModule } from 'primeng/table';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { IconFieldModule } from 'primeng/iconfield';
import { InputIconModule } from 'primeng/inputicon';
import { AuditTrailService } from '../../services/audit-trail.service';
import { AuditTrail } from '../../models/audit-trail.model';

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
        DatePipe
    ],
    templateUrl: './audit-trail-list.component.html'
})
export class AuditTrailListComponent {
    private readonly auditTrailService = inject(AuditTrailService);

    auditTrails = signal<AuditTrail[]>([]);
    totalRecords = signal<number>(0);
    loading = signal<boolean>(true);
    pageSize = signal<number>(10);
    searchTerm = '';

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

        this.auditTrailService.getAuditTrails({
            page,
            size,
            sort,
            content: this.searchTerm || undefined
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

    getSeverity(event: string): 'success' | 'info' | 'warn' | 'danger' | 'secondary' {
        switch (event) {
            case 'CREATED': return 'success';
            case 'UPDATED': return 'info';
            case 'VALIDATED': return 'success';
            case 'DELETED': case 'ARCHIVED': return 'danger';
            default: return 'secondary';
        }
    }
}
