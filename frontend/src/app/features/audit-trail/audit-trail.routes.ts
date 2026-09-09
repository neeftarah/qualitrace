import { Routes } from '@angular/router';
import { roleGuard } from '../../core/guards/role.guard';

export const AUDIT_TRAIL_ROUTES: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('./pages/audit-trail-list/audit-trail-list.component').then(
                (m) => m.AuditTrailListComponent
            ),
        canActivate: [roleGuard(['ADMIN', 'AQ'])]
    }
];
