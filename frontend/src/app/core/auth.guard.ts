import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = async (_, state) => {
    const auth = inject(AuthService);
    const router = inject(Router);

    // Si le profil n'est pas encore en mémoire mais qu'on a un jeton, on attend l'initialisation
    if (!auth.currentUser() && auth.getToken()) {
        await auth.initializeAuth();
    }

    if (auth.currentUser()) {
        return true;
    }

    return router.createUrlTree(['/auth/login'], { queryParams: { returnUrl: state.url } });
};
