import { Component } from '@angular/core';
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';

@Component({
    selector: 'app-dashboard',
    imports: [CardModule, ButtonModule],
    template: `
        <div class="flex flex-col gap-8">
            <div>
                <h1 class="text-3xl font-semibold text-surface-900 dark:text-surface-0 mb-2">Bienvenue sur QualiTrace</h1>
                <p class="text-muted-color text-lg m-0">Pilotez la qualité et la traçabilité de vos lots.</p>
            </div>
            <div class="grid grid-cols-12 gap-6">
                <p-card class="col-span-12 md:col-span-4" header="Lots">
                    <p class="text-muted-color">Consultez les lots réceptionnés et leur statut de validation.</p>
                    <p-button label="Voir les lots" icon="pi pi-box" disabled />
                </p-card>
                <p-card class="col-span-12 md:col-span-4" header="Contrôles qualité">
                    <p class="text-muted-color">Retrouvez les spécifications et résultats d’analyses.</p>
                    <p-button label="Voir les contrôles" icon="pi pi-check-circle" severity="secondary" disabled />
                </p-card>
                <p-card class="col-span-12 md:col-span-4" header="Déviations">
                    <p class="text-muted-color">Suivez les écarts ouverts et leur clôture.</p>
                    <p-button label="Voir les déviations" icon="pi pi-exclamation-triangle" severity="warn" disabled />
                </p-card>
            </div>
        </div>
    `
})
export class Dashboard {}
