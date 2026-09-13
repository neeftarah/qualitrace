import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { PanelModule } from 'primeng/panel';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';

import { BatchService } from '@/app/features/batch/services/batch.service';
import { Batch, BatchSpecification, BatchDeviation } from '@/app/features/batch/models/batch.model';
import { AuthService } from '@/app/core/auth.service';
import {HttpResponse} from "@angular/common/http";

@Component({
    selector: 'app-batch-detail',
    standalone: true,
    imports: [CommonModule, FormsModule, PanelModule, TableModule, TagModule, ButtonModule, InputNumberModule],
    templateUrl: './batch-detail.component.html'
})
export class BatchDetailComponent implements OnInit {
    private readonly route = inject(ActivatedRoute);
    private readonly batchService = inject(BatchService);
    private readonly authService = inject(AuthService);
    private readonly location = inject(Location);

    batch = signal<Batch | undefined>(undefined);
    expandedDeviations = signal<Set<number>>(new Set());
    editingValues: Record<number, number> = {};

    ngOnInit(): void {
        const id = Number(this.route.snapshot.paramMap.get('id'));
        this.load(id);
    }

    load(id: number): void {
        this.batchService.getById(id).subscribe({
            next: (batch) => this.batch.set(batch),
            error: (err) => console.error('Erreur chargement lot', err)
        });
    }

    get canSeeMinMax(): boolean {
        return this.authService.hasAnyRole(['ADMIN', 'AQ']);
    }

    get canSeeDeviations(): boolean {
        return this.authService.hasAnyRole(['ADMIN', 'AQ']);
    }

    get canEditAnalysis(): boolean {
        return this.authService.hasAnyRole(['CQ']);
    }

    toggleDeviationExpand(id: number): void {
        const current = new Set(this.expandedDeviations());
        current.has(id) ? current.delete(id) : current.add(id);
        this.expandedDeviations.set(current);
    }

    isExpanded(id: number): boolean {
        return this.expandedDeviations().has(id);
    }

    saveResult(spec: BatchSpecification): void {
        const value = this.editingValues[spec.id];
        const currentBatch = this.batch();
        if (value === undefined || !currentBatch) return;

        const request$ = spec.results
            ? this.batchService.updateAnalysis(currentBatch.id, spec.results.id, value)
            : this.batchService.saveAnalysis(currentBatch.id, spec.id, value);

        request$.subscribe({
            next: () => this.load(currentBatch.id),
            error: (err) => console.error('Erreur enregistrement résultat', err)
        });
    }

    toggleDeviationStatus(dev: BatchDeviation): void {
        const currentBatch = this.batch();
        if (!currentBatch) return;
        const action = dev.status === 'OPENED' ? 'close' : 'open';
        this.batchService.toggleDeviation(currentBatch.id, dev.id, action).subscribe({
            next: () => this.load(currentBatch.id),
            error: (err) => console.error('Erreur maj déviation', err)
        });
    }

    getTypeName(type: string): string {
        switch (type) {
            case 'RAW_MATERIAL' :
                return 'Matières premières';
            case 'COMPONENT' :
                return 'Composants d\'emballage';
            default:
                return type;
        }
    }

    goBack(): void {
        this.location.back();
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
                return 'QUARANTAINE';
            case 'RELEASED':
                return 'LIBÉRÉ';
            case 'USED':
                return 'UTILISÉ';
            case 'REJECTED':
                return 'REJETÉ';
            case 'DESTROYED':
                return 'DÉTRUIT';
            default:
                return type;
        }
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
