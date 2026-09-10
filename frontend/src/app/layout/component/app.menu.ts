import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MenuItem } from 'primeng/api';
import { AppMenuitem } from './app.menuitem';
import { AuthService } from '../../core/auth.service';

@Component({
    selector: 'app-menu',
    standalone: true,
    imports: [CommonModule, AppMenuitem, RouterModule],
    template: `<ul class="layout-menu">
        @for (item of model; track item.label) {
            @if (!item.separator) {
                <li app-menuitem [item]="item" [root]="true"></li>
            } @else {
                <li class="menu-separator"></li>
            }
        }
    </ul> `,
})
export class AppMenu {
    private readonly auth = inject(AuthService);
    model: MenuItem[] = [];

    ngOnInit() {
        const baseMenu: MenuItem[] = [
            {
                label: 'Home',
                items: [{ label: 'Tableau de bord', icon: 'pi pi-fw pi-home', routerLink: ['/'] }]
            }
        ];

        if (this.auth.hasAnyRole(['ADMIN'])) {
            baseMenu.push({
                label: 'Référentiels',
                items: [
                    { label: 'Fournisseurs', icon: 'pi pi-fw pi-truck', routerLink: ['/suppliers'] },
                    { label: 'Composants', icon: 'pi pi-fw pi-box', routerLink: ['/components'] },
                    { label: 'utilisateurs', icon: 'pi pi-fw pi-user', routerLink: ['/users'] },
                ]
            });
        }

        baseMenu.push({
            label: 'Fabrication',
            items: [
                { label: 'Gestion des lots', icon: 'pi pi-fw pi-gift', routerLink: ['/batches'] },
            ]
        });

        if (this.auth.hasAnyRole(['ADMIN', 'AQ'])) {
            baseMenu.push({
                label: 'Traçabilité & Conformité',
                items: [
                    {
                        label: 'Audit Trail',
                        icon: 'pi pi-fw pi-history',
                        routerLink: ['/audit-trail']
                    }
                ]
            });
        }

        baseMenu.push({
            label: 'Documentation',
            items: [
            {
                label: 'Sakai - PrimeNG',
                icon: 'pi pi-fw pi-book',
                url: 'https://sakai.primeng.org/documentation',
                target: '_blank'
            }
        ]
        });

        this.model = baseMenu;
    }
}
