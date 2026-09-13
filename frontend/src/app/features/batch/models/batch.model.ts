export type BatchStatus = 'QUARANTINE' | 'RELEASED' | 'REJECTED' | 'USED' | 'DESTROYED';

export interface ComponentSupplier {
    id: number;
    code: string;
    name: string;
    address: string;
    status: string;
}

export interface BatchComponent {
    id: number;
    type: 'RAW_MATERIAL' | 'COMPONENT' | string;
    reference: string;
    name: string;
    availableFrom: string | null;
    status: string;
    supplier: ComponentSupplier;
}

export interface Batch {
    id: number;
    component: BatchComponent;
    internalBatchNumber: string;
    supplierBatchNumber: string;
    expiryDate: string;
    receptionDate: string;
    status: BatchStatus;
}

export interface HalPage {
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

export interface BatchHalResponse {
    _embedded?: {
        batches: Batch[];
    };
    page: HalPage;
}

export interface BatchQueryParams {
    page?: number;
    size?: number;
    sort?: string;
    internalBatchNumber?: string;
    componentId?: string;
    supplierId?: string;
    supplierBatchNumber?: string;
    expiryFromDate?: string;
    expiryToDate?: string;
    receptionFromDate?: string;
    receptionToDate?: string;
    status?: string;
    validatedBy?: string;
    validationFromDate?: string;
    validationToDate?: string;
}
