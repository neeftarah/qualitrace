import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';

export interface LoginResponse { token: string; }

@Injectable({ providedIn: 'root' })
export class AuthService {
    private readonly http = inject(HttpClient);
    private readonly apiUrl = 'http://localhost:8080/api/v1';
    private readonly storageKey = 'qualitrace.authenticated';
    readonly isAuthenticated = signal(sessionStorage.getItem(this.storageKey) === 'true');

    login(login: string, password: string): Observable<LoginResponse> {
        return this.http.post<LoginResponse>(`${this.apiUrl}/auth/login`, { login, password }).pipe(
            tap(() => {
                sessionStorage.setItem(this.storageKey, 'true');
                this.isAuthenticated.set(true);
            })
        );
    }

    logout(): Observable<void> {
        return this.http.post<void>(`${this.apiUrl}/auth/logout`, {}).pipe(tap(() => this.clearSession()));
    }

    clearSession(): void {
        sessionStorage.removeItem(this.storageKey);
        this.isAuthenticated.set(false);
    }
}
