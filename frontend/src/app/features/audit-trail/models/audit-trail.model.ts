export interface AuditAuthor {
    id: string;
    login: string;
    email: string;
    firstname: string;
    surname: string;
    status: string;
    roles: string[];
    createdAt: string;
    updatedAt: string | null;
}

export interface AuditTrail {
    id: number;
    author: AuditAuthor;
    event: string;
    entity_type: string;
    entity_id: string;
    timestamp: string;
    previous_data: string;
    changed_data: string;
}

export interface HalPage {
    size: number;
    totalElements: number;
    totalPages: number;
    number: number;
}

export interface AuditTrailHalResponse {
    _embedded: {
        audit_trails: AuditTrail[];
    };
    page: HalPage;
}

export interface AuditTrailQueryParams {
    page?: number;
    size?: number;
    sort?: string;
    content?: string;
    event?: string;
    entity_type?: string;
    entity_id?: string;
    author_id?: string;
    fromDate?: string;
    toDate?: string;
}
