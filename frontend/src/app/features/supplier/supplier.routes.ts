import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role.guard';

export const SUPPLIER_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('./pages/supplier-list/supplier-list.component').then(
                (m) => m.SupplierListComponent
            ),
        canActivate: [roleGuard(['ADMIN'])]
    }
];
