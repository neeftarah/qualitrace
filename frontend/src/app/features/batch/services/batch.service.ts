import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams, HttpResponse} from '@angular/common/http';
import {map, Observable} from 'rxjs';
import {environment} from '../../../../environments/environment';
import {Batch, BatchHalResponse, BatchQueryParams} from '../models/batch.model';
import {SupplierHalResponse, SupplierOption} from "@/app/features/supplier/models/supplier.model";
import {ComponentHalResponse, ComponentOption} from "@/app/features/component/models/component.model";

@Injectable({
    providedIn: 'root'
})
export class BatchService {
    private readonly http = inject(HttpClient);
    private readonly apiUrl = `${environment.apiUrl}/batches`;

    getBatchs(params: BatchQueryParams): Observable<BatchHalResponse> {
        let httpParams = new HttpParams();

        if (params.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
        if (params.size !== undefined) httpParams = httpParams.set('size', params.size.toString());
        if (params.sort) httpParams = httpParams.set('sort', params.sort);
        if (params.internalBatchNumber) httpParams = httpParams.set('internalBatchNumber', params.internalBatchNumber.toString());
        if (params.componentId) httpParams = httpParams.set('componentId', params.componentId);
        if (params.supplierId) httpParams = httpParams.set('supplierId', params.supplierId);
        if (params.supplierBatchNumber) httpParams = httpParams.set('supplierBatchNumber', params.supplierBatchNumber.toString());
        if (params.expiryFromDate) httpParams = httpParams.set('expiryFromDate', params.expiryFromDate.toString());
        if (params.receptionFromDate) httpParams = httpParams.set('receptionFromDate', params.receptionFromDate.toString());
        if (params.receptionToDate) httpParams = httpParams.set('receptionToDate', params.receptionToDate.toString());
        if (params.expiryToDate) httpParams = httpParams.set('expiryToDate', params.expiryToDate.toString());
        if (params.status) httpParams = httpParams.set('status', params.status.toString());
        if (params.validatedBy) httpParams = httpParams.set('validatedBy', params.validatedBy);
        if (params.validationFromDate) httpParams = httpParams.set('validationFromDate', params.validationFromDate.toString());
        if (params.validationToDate) httpParams = httpParams.set('validationToDate', params.validationToDate.toString());

        return this.http.get<BatchHalResponse>(this.apiUrl, {params: httpParams});
    }

    getBatchById(id: number): Observable<Batch> {
        return this.http.get<Batch>(this.apiUrl + '/' + id);
    }

    destroyBatch(item: Batch) {
        return this.http.patch<BatchHalResponse>(this.apiUrl + '/' + item.id + '/destroy', null);
    }

    validateBatch(item: Batch) {
        return this.http.patch<BatchHalResponse>(this.apiUrl + '/' + item.id + '/validate', null);
    }

    useBatch(item: Batch) {
        return this.http.patch<BatchHalResponse>(this.apiUrl + '/' + item.id + '/use', null);
    }

    getCertificateBatch(item: Batch): Observable<HttpResponse<Blob>> {
        return this.http.get(this.apiUrl + '/' + item.id + '/certificate', {
            responseType: 'blob',
            observe: 'response'
        });
    }

    getComponents(): Observable<ComponentOption[]> {
        return this.http.get<ComponentHalResponse>('/api/v1/components?size=100&sort=name,asc').pipe(
            map(response => response._embedded?.components || [])
        );
    }

    getSuppliers(): Observable<SupplierOption[]> {
        return this.http.get<SupplierHalResponse>('/api/v1/suppliers?size=100&sort=name,asc').pipe(
            map(response => response._embedded?.suppliers || [])
        );
    }
}
