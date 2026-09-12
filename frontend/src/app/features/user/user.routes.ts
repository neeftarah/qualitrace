import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role.guard';

export const USER_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('./pages/user-list/user-list.component').then(
                (m) => m.UserListComponent
            ),
        canActivate: [roleGuard(['ADMIN'])]
    }
];
