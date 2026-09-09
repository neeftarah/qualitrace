import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
    const router = inject(Router);
    const token = localStorage.getItem('qualitrace.token');

    let req = request.clone({ withCredentials: true });

    if (token) {
        req = req.clone({
            setHeaders: { Authorization: `Bearer ${token}` }
        });
    }

    return next(req).pipe(
        catchError((error: HttpErrorResponse) => {
            if (error.status === 401 && !request.url.endsWith('/auth/login')) {
                localStorage.removeItem('qualitrace.token');
                void router.navigate(['/auth/login']);
            }
            return throwError(() => error);
        })
    );
};
