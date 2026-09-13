export interface ComponentOption {
    id: string;
    reference: string;
    name: string;
}

export interface ComponentSupplier {
    id: string;
    code: string;
    name: string;
    address: string;
    status: string;
}

export interface Component {
    id: string;
    type: string;
    reference: string;
    name: string;
    status: string;
    availableFrom: string | null;
    supplier: ComponentSupplier[];
}

export interface HalPage {
    size: number;
    totalElements: number;
    totalPages: number;
    number: number;
}

export interface ComponentHalResponse {
    _embedded: {
        components: Component[];
    };
    page: HalPage;
}

export interface ComponentQueryParams {
    page?: number;
    size?: number;
    sort?: string;
    type?: string;
    reference?: string;
    name?: string;
    status?: string;
    supplierId?: string;
}
