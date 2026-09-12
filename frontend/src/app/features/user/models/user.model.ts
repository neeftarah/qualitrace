export interface UserOption {
    id: string;
    login: string;
    firstname: string;
    surname: string;
}

export interface User {
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

export interface HalPage {
    size: number;
    totalElements: number;
    totalPages: number;
    number: number;
}

export interface UserHalResponse {
    _embedded: {
        users: User[];
    };
    page: HalPage;
}

export interface UserQueryParams {
    page?: number;
    size?: number;
    sort?: string;
    login?: string;
    email?: string;
    firstname?: string;
    surname?: string;
    role?: string;
    status?: string;
    fromCreationDate?: string;
    toCreationDate?: string;
    fromUpdateDate?: string;
    toUpdateDate?: string;
}

