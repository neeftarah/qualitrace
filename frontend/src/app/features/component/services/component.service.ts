import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {map, Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {Component, ComponentHalResponse, ComponentQueryParams} from '../models/component.model';
import {SupplierHalResponse, SupplierOption} from "@/app/features/supplier/models/supplier.model";

@Injectable({
    providedIn: 'root'
})
export class ComponentService {
    private readonly http = inject(HttpClient);
    private readonly apiUrl = `${environment.apiUrl}/components`;

    getComponents(params: ComponentQueryParams): Observable<ComponentHalResponse> {
        let httpParams = new HttpParams();

        if (params.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
        if (params.size !== undefined) httpParams = httpParams.set('size', params.size.toString());
        if (params.sort) httpParams = httpParams.set('sort', params.sort);
        if (params.type) httpParams = httpParams.set('type', params.type.toString());
        if (params.reference) httpParams = httpParams.set('reference', params.reference.toString());
        if (params.name) httpParams = httpParams.set('name', params.name.toString());
        if (params.status) httpParams = httpParams.set('status', params.status.toString());
        if (params.supplierId) httpParams = httpParams.set('supplierId', params.supplierId);

        return this.http.get<ComponentHalResponse>(this.apiUrl, {params: httpParams});
    }

    archiveComponent(item: Component) {
        return this.http.patch<ComponentHalResponse>(this.apiUrl + '/' + item.id + '/archive', null);
    }

    activateComponent(item: Component) {
        return this.http.patch<ComponentHalResponse>(this.apiUrl + '/' + item.id + '/activate', null);
    }

    getSuppliers(): Observable<SupplierOption[]> {
        return this.http.get<SupplierHalResponse>('/api/v1/suppliers?size=100&sort=name,asc').pipe(
            map(response => response._embedded?.suppliers || [])
        );
    }
}
