import { Routes } from '@angular/router';

export const BATCH_ROUTES: Routes = [
    {
        path: ':id',
        loadComponent: () =>
            import('./pages/batch-detail/batch-detail.component').then((m) => m.BatchDetailComponent)
    },
    {
        path: '',
        loadComponent: () =>
            import('./pages/batch-list/batch-list.component').then((m) => m.BatchListComponent)
    }
];
