export interface Supplier {
    id: string;
    code: string;
    name: string;
    address: string;
    status: string;
}

export interface HalPage {
    size: number;
    totalElements: number;
    totalPages: number;
    number: number;
}

export interface SupplierHalResponse {
    _embedded: {
        suppliers: Supplier[];
    };
    page: HalPage;
}

export interface SupplierQueryParams {
    page?: number;
    size?: number;
    sort?: string;
    code?: string;
    name?: string;
    status?: string;
}
