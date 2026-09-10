import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AuditTrailHalResponse, AuditTrailQueryParams } from '../models/audit-trail.model';
import { UserOption, UserHalResponse } from '../../user/models/user.model';

@Injectable({
    providedIn: 'root'
})
export class AuditTrailService {
    private readonly http = inject(HttpClient);
    private readonly apiUrl = `${environment.apiUrl}/audit_trail`;

    getAuditTrails(params: AuditTrailQueryParams): Observable<AuditTrailHalResponse> {
        let httpParams = new HttpParams();

        if (params.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
        if (params.size !== undefined) httpParams = httpParams.set('size', params.size.toString());
        if (params.sort) httpParams = httpParams.set('sort', params.sort);
        if (params.content) httpParams = httpParams.set('content', params.content);
        if (params.event) httpParams = httpParams.set('event', params.event);
        if (params.entity_type) httpParams = httpParams.set('entity_type', params.entity_type);
        if (params.entity_id) httpParams = httpParams.set('entity_id', params.entity_id);
        if (params.author_id) httpParams = httpParams.set('author_id', params.author_id);
        if (params.fromDate) httpParams = httpParams.set('fromDate', params.fromDate);
        if (params.toDate) httpParams = httpParams.set('toDate', params.toDate);

        return this.http.get<AuditTrailHalResponse>(this.apiUrl, { params: httpParams });
    }

    getAuthors(): Observable<UserOption[]> {
        return this.http.get<UserHalResponse>('/api/v1/users?size=100&sort=surname,asc').pipe(
            map(response => response._embedded?.users || [])
        );
    }
}
