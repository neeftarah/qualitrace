import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, switchMap, of, catchError, firstValueFrom } from 'rxjs';
import { UserSelf } from './user.model';
import { environment } from '../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private readonly http = inject(HttpClient);
    private readonly tokenKey = 'qualitrace.token';

    // Signal contenant le profil de l'utilisateur connecté
    currentUser = signal<UserSelf | null>(null);

    login(login: string, password: string): Observable<UserSelf> {
        return this.http.post<{ token: string }>(`${environment.apiUrl}/auth/login`, { login, password }).pipe(
            tap(res => localStorage.setItem(this.tokenKey, res.token)),
            switchMap(() => this.fetchCurrentUser())
        );
    }

    logout(): Observable<void> {
            return this.http.post<void>(`${environment.apiUrl}/auth/logout`, {}).pipe(tap(() => this.clearSession()));
    }

    fetchCurrentUser(): Observable<UserSelf> {
        return this.http.get<UserSelf>(`${environment.apiUrl}/users/self`).pipe(
            tap(user => this.currentUser.set(user))
        );
    }

    getToken(): string | null {
        return localStorage.getItem(this.tokenKey);
    }

    clearSession(): void {
        localStorage.removeItem(this.tokenKey);
        this.currentUser.set(null);
    }

    hasAnyRole(requiredRoles: string[]): boolean {
        const user = this.currentUser();
        if (!user || !user.roles) return false;
        return requiredRoles.some(role => user.roles.includes(role));
    }

    async initializeAuth(): Promise<UserSelf | null> {
        const token = this.getToken();
        if (!token) {
            return null;
        }

        try {
            const user = await firstValueFrom(this.fetchCurrentUser());
            console.log('Utilisateur réhydraté au F5 :', user);
            return user;
        } catch (error) {
            console.error('Échec réhydratation au F5 :', error);
            this.clearSession();
            return null;
        }
    }
}
