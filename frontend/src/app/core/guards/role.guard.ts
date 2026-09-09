import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../auth.service';

export const roleGuard = (allowedRoles: string[]): CanActivateFn => {
    return () => {
        const auth = inject(AuthService);
        const router = inject(Router);

        if (auth.hasAnyRole(allowedRoles)) {
            return true;
        }

        void router.navigate(['/']);
        return false;
    };
};
