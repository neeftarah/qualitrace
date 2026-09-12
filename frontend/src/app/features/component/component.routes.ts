import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role.guard';

export const COMPONENT_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('./pages/component-list/component-list.component').then(
                (m) => m.ComponentListComponent
            ),
        canActivate: [roleGuard(['ADMIN'])]
    }
];
