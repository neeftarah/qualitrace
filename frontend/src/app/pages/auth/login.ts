import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { CheckboxModule } from 'primeng/checkbox';
import { InputTextModule } from 'primeng/inputtext';
import { PasswordModule } from 'primeng/password';
import { RippleModule } from 'primeng/ripple';
import { AppFloatingConfigurator } from '../../layout/component/app.floatingconfigurator';
import { AuthService } from '../../core/auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [ButtonModule, CheckboxModule, InputTextModule, PasswordModule, FormsModule, RouterModule, RippleModule, AppFloatingConfigurator],
    template: `
        <app-floating-configurator />
        <div class="bg-surface-50 dark:bg-surface-950 flex items-center justify-center min-h-screen min-w-screen overflow-hidden">
            <div class="flex flex-col items-center justify-center">
                <div style="border-radius: 56px; padding: 0.3rem; background: linear-gradient(180deg, var(--primary-color) 10%, rgba(33, 150, 243, 0) 30%)">
                    <div class="w-full bg-surface-0 dark:bg-surface-900 py-20 px-8 sm:px-20" style="border-radius: 53px">
                        <div class="text-center mb-8">
                            <img src="logo-qualitrace.png" alt="QualiTrace" title="" class="h-48 w-auto m-auto mb-6" />
                            <div class="text-surface-900 dark:text-surface-0 text-3xl font-medium mb-4">Bienvenue sur QualiTrace</div>
                            <span class="text-muted-color font-medium">Connectez-vous pour continuer</span>
                        </div>

                        <div>
                            <form (ngSubmit)="submit()">
                                <label for="login" class="block text-surface-900 dark:text-surface-0 text-xl font-medium mb-2">Identifiant</label>
                                <input pInputText id="login" type="text" placeholder="Votre identifiant" class="w-full md:w-120 mb-8" [(ngModel)]="login" name="login" />

                                <label for="password" class="block text-surface-900 dark:text-surface-0 font-medium text-xl mb-2">Mot de passe</label>
                                <p-password inputId="password" [(ngModel)]="password" name="password" placeholder="Votre mot de passe" [toggleMask]="true" styleClass="mb-4" [fluid]="true" [feedback]="false"></p-password>

                                <div class="flex items-center justify-between mt-2 mb-8 gap-8">
                                    <div class="flex items-center">
                                        <p-checkbox [(ngModel)]="checked" inputId="rememberme" name="rememberme" binary class="mr-2" disabled="disabled"></p-checkbox>
                                        <label for="rememberme">Remember me</label>
                                    </div>
                                    <span class="font-medium no-underline ml-2 text-right cursor-pointer text-primary">Forgot password?</span>
                                </div>
                                @if (errorMessage()) {
                                    <div class="text-red-500 mb-4">{{ errorMessage() }}</div>
                                }
                                <p-button type="submit" label="Se connecter" icon="pi pi-sign-in" styleClass="w-full" [loading]="loading()"></p-button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `
})
export class Login {
    private readonly auth = inject(AuthService);
    private readonly router = inject(Router);
    private readonly route = inject(ActivatedRoute);
    login = '';
    password = '';
    checked: boolean = false;
    loading = signal<boolean>(false);
    errorMessage = signal<string>('');

    submit(): void {
        if (!this.login || !this.password) return;
        this.loading.set(true);
        this.errorMessage.set('');
        this.auth.login(this.login, this.password).subscribe({
            next: () => void this.router.navigateByUrl(this.route.snapshot.queryParamMap.get('returnUrl') || '/'),
            error: (err: HttpErrorResponse) => {
                this.loading.set(false);
                if (err.status === 401) {
                    this.errorMessage.set('Identifiant ou mot de passe incorrect.');
                } else {
                    this.errorMessage.set('Une erreur réseau ou serveur est survenue.');
                }
            }
        });
    }
}
