import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
    const auth = inject(AuthService);
    const router = inject(Router);
    return next(request.clone({ withCredentials: true })).pipe(
        catchError((error: HttpErrorResponse) => {
            if (error.status === 401 && !request.url.endsWith('/auth/login')) {
                auth.clearSession();
                void router.navigate(['/auth/login']);
            }
            return throwError(() => error);
        })
    );
};
