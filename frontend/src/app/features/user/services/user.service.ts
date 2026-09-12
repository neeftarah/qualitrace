import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {User, UserHalResponse, UserQueryParams} from '../models/user.model';

@Injectable({
    providedIn: 'root'
})
export class UserService {
    private readonly http = inject(HttpClient);
    private readonly apiUrl = `${environment.apiUrl}/users`;

    getUsers(params: UserQueryParams): Observable<UserHalResponse> {
        let httpParams = new HttpParams();

        if (params.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
        if (params.size !== undefined) httpParams = httpParams.set('size', params.size.toString());
        if (params.sort) httpParams = httpParams.set('sort', params.sort);

        if (params.login) httpParams = httpParams.set('login', params.login.toString());
        if (params.email) httpParams = httpParams.set('email', params.email.toString());
        if (params.firstname) httpParams = httpParams.set('firstname', params.firstname.toString());
        if (params.surname) httpParams = httpParams.set('surname', params.surname.toString());
        if (params.role) httpParams = httpParams.set('role', params.role.toString());
        if (params.status) httpParams = httpParams.set('status', params.status.toString());
        if (params.fromCreationDate) httpParams = httpParams.set('fromCreationDate', params.fromCreationDate.toString());
        if (params.toCreationDate) httpParams = httpParams.set('toCreationDate', params.toCreationDate.toString());
        if (params.fromUpdateDate) httpParams = httpParams.set('fromUpdateDate', params.fromUpdateDate.toString());
        if (params.toUpdateDate) httpParams = httpParams.set('toUpdateDate', params.toUpdateDate.toString());

        return this.http.get<UserHalResponse>(this.apiUrl, {params: httpParams});
    }

    archiveUser(item: User) {
        return this.http.patch<UserHalResponse>(this.apiUrl + '/' + item.id + '/archive', null);
    }

    activateUser(item: User) {
        return this.http.patch<UserHalResponse>(this.apiUrl + '/' + item.id + '/activate', null);
    }

    unlockUser(item: User) {
        return this.http.patch<UserHalResponse>(this.apiUrl + '/' + item.id + '/unlock', null);
    }
}
