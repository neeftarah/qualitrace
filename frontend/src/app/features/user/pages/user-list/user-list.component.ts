import {Component, inject, signal} from '@angular/core';
import {CommonModule, formatDate} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Table, TableLazyLoadEvent, TableModule} from 'primeng/table';
import {InputTextModule} from 'primeng/inputtext';
import {ButtonModule} from 'primeng/button';
import {TagModule} from 'primeng/tag';
import {IconFieldModule} from 'primeng/iconfield';
import {InputIconModule} from 'primeng/inputicon';
import {DatePickerModule} from 'primeng/datepicker';
import {SelectModule} from 'primeng/select';
import {UserService} from '../../services/user.service';
import {User} from '../../models/user.model';
import {DialogModule} from 'primeng/dialog';
import {TooltipModule} from 'primeng/tooltip';
import {ConfirmDialogModule} from 'primeng/confirmdialog';
import {ConfirmationService} from 'primeng/api';
import {Toolbar} from "primeng/toolbar";

@Component({
    selector: 'user-list',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        TableModule,
        InputTextModule,
        ButtonModule,
        TagModule,
        Toolbar,
        IconFieldModule,
        InputIconModule,
        DatePickerModule,
        SelectModule,
        DialogModule,
        ButtonModule,
        TooltipModule,
        ConfirmDialogModule
    ],
    providers: [ConfirmationService],
    templateUrl: './user-list.component.html'
})
export class UserListComponent {
    private readonly userService = inject(UserService);
    private confirmationService = inject(ConfirmationService);

    users = signal<User[]>([]);
    totalRecords = signal<number>(0);
    loading = signal<boolean>(true);
    pageSize = signal<number>(10);

    // Critères de recherche
    searchLogin: string | null = null;
    searchEmail: string | null = null;
    searchFirstname: string | null = null;
    searchSurname: string | null = null;
    dateCreationRange: Date[] | null = null;
    dateUpdateRange: Date[] | null = null;
    selectedRole: string | null = null;
    selectedStatus: string | null = null;

    statusOptions = [
        {label: 'Tous les statuts', value: null},
        {label: 'Actifs', value: 'ACTIVE'},
        {label: 'Archivés', value: 'ARCHIVED'},
        {label: 'Bloqués', value: 'LOCKED'}
    ];

    rolesOptions = [
        {label: 'Tous les rôles', value: null},
        { label: 'ADMIN', value: 'ADMIN' },
        { label: 'AQ', value: 'AQ' },
        { label: 'CQ', value: 'CQ' },
        { label: 'SUPPLY', value: 'SUPPLY' },
        { label: 'PLANNING', value: 'PLANNING' },
        { label: 'PRODUCTION', value: 'PRODUCTION' }
    ];

    loadData(event: TableLazyLoadEvent): void {
        this.loading.set(true);

        const page = (event.first ?? 0) / (event.rows ?? 10);
        const size = event.rows ?? 10;

        let sort: string | undefined = undefined;
        if (event.sortField) {
            const field = Array.isArray(event.sortField) ? event.sortField[0] : event.sortField;
            const order = event.sortOrder === 1 ? 'asc' : 'desc';
            sort = `${field},${order}`;
        }

        let fromCreationDate: string | undefined = undefined;
        if (this.dateCreationRange && this.dateCreationRange[0]) {
            fromCreationDate = formatDate(this.dateCreationRange[0], 'yyyy-MM-dd', 'en-US');
        }

        let toCreationDate: string | undefined = undefined;
        if (this.dateCreationRange && this.dateCreationRange[1]) {
            toCreationDate = formatDate(this.dateCreationRange[1], 'yyyy-MM-dd', 'en-US');
        }

        let fromUpdateDate: string | undefined = undefined;
        if (this.dateUpdateRange && this.dateUpdateRange[0]) {
            fromUpdateDate = formatDate(this.dateUpdateRange[0], 'yyyy-MM-dd', 'en-US');
        }

        let toUpdateDate: string | undefined = undefined;
        if (this.dateUpdateRange && this.dateUpdateRange[1]) {
            toUpdateDate = formatDate(this.dateUpdateRange[1], 'yyyy-MM-dd', 'en-US');
        }

        this.userService.getUsers({
            page,
            size,
            sort,
            login: this.searchLogin || undefined,
            email: this.searchEmail || undefined,
            firstname: this.searchFirstname || undefined,
            surname: this.searchSurname || undefined,
            role: this.selectedRole || undefined,
            status: this.selectedStatus || undefined,
            fromCreationDate,
            toCreationDate,
            fromUpdateDate,
            toUpdateDate
        }).subscribe({
            next: (res) => {
                this.users.set(res._embedded?.users || []);
                this.totalRecords.set(res.page?.totalElements || 0);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }

    applyFilters(dt: Table): void {
        dt.reset();
    }

    resetFilters(dt: Table): void {
        this.searchLogin = null;
        this.searchEmail = null;
        this.searchFirstname = null;
        this.searchSurname = null;
        this.selectedRole = null;
        this.dateCreationRange = null;
        this.dateUpdateRange = null;
        this.selectedStatus = null;
        dt.reset();
    }

    getStatusClass(event: string): 'success' | 'danger' | 'contrast' | 'secondary' {
        switch (event) {
            case 'ACTIVE':
                return 'success';
            case 'ARCHIVED':
                return 'danger';
            case 'LOCKED':
                return 'contrast';
            default:
                return 'secondary';
        }
    }

    getRoleClass(role: string): 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast' {
        switch (role) {
            case 'ADMIN': return 'contrast';
            case 'AQ': return 'danger';
            case 'CQ': return 'warn';
            case 'SUPPLY': return 'info';
            case 'PLANNING': return 'success';
            case 'PRODUCTION': return 'secondary';
            default: return 'secondary';
        }
    }

    displayModal = signal<boolean>(false);

    openModal(item: User | null): void {
        this.displayModal.set(true);
    }

    archiveUser(item: User) {
        this.confirmationService.confirm({
            header: 'Êtes-vous sûr de vouloir archiver cet utilisateur ?',
            message: 'L\'utilisateur ne pourra plus se connecter mais toutes les données qui lui sont rattachées resteront disponible.',
            icon: 'pi pi-info-circle',
            acceptLabel: 'Confirmer',
            rejectLabel: 'Annuler',
            acceptButtonStyleClass: 'p-button-danger',
            rejectButtonStyleClass: 'p-button-secondary p-button-text',
            accept: () => {
                this.userService.archiveUser(item).subscribe({
                    next: () => {
                        this.users.update(list =>
                            list.map(s => s.id === item.id ? {...s, status: 'ARCHIVED'} : s)
                        );
                    },
                    error: (err) => {
                        console.error('Erreur lors de l\'archivage de l\'utilisateur', err);
                    }
                });
            }
        });
    }

    activateUser(item: User) {
        this.userService.activateUser(item).subscribe({
            next: () => {
                this.users.update(list =>
                    list.map(s => s.id === item.id ? {...s, status: 'ACTIVE'} : s)
                );
            },
            error: (err) => {
                console.error('Erreur lors de la réactivation de l\'utilisateur', err);
            }
        });
    }

    unlockUser(item: User) {
        this.userService.unlockUser(item).subscribe({
            next: () => {
                this.users.update(list =>
                    list.map(s => s.id === item.id ? {...s, status: 'ACTIVE'} : s)
                );
            },
            error: (err) => {
                console.error('Erreur lors de la réactivation de l\'utilisateur', err);
            }
        });
    }
}
