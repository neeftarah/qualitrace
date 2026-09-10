export interface UserOption {
    id: string;
    login: string;
    firstname: string;
    surname: string;
}

export interface UserHalResponse {
    _embedded?: {
        users: UserOption[];
    };
}
