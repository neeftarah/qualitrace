import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {Supplier, SupplierHalResponse, SupplierQueryParams} from '../models/supplier.model';

@Injectable({
    providedIn: 'root'
})
export class SupplierService {
    private readonly http = inject(HttpClient);
    private readonly apiUrl = `${environment.apiUrl}/suppliers`;

    getSuppliers(params: SupplierQueryParams): Observable<SupplierHalResponse> {
        let httpParams = new HttpParams();

        if (params.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
        if (params.size !== undefined) httpParams = httpParams.set('size', params.size.toString());
        if (params.sort) httpParams = httpParams.set('sort', params.sort);
        if (params.code) httpParams = httpParams.set('code', params.code.toString());
        if (params.name) httpParams = httpParams.set('name', params.name.toString());
        if (params.status) httpParams = httpParams.set('status', params.status.toString());

        return this.http.get<SupplierHalResponse>(this.apiUrl, {params: httpParams});
    }

    archiveSupplier(item: Supplier) {
        return this.http.patch<SupplierHalResponse>(this.apiUrl + '/' + item.id + '/archive', null);
    }

    activateSupplier(item: Supplier) {
        return this.http.patch<SupplierHalResponse>(this.apiUrl + '/' + item.id + '/activate', null);
    }
}
