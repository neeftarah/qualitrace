import {Routes} from '@angular/router';

export const BATCH_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('./pages/batch-list/batch-list.component').then((m) => m.BatchListComponent)
    }
];
