CREATE TABLE users
(
    id         UUID                        NOT NULL
        CONSTRAINT pk_users PRIMARY KEY,
    login      VARCHAR(255)                NOT NULL
        CONSTRAINT uk_users_login UNIQUE,
    password   VARCHAR(255)                NOT NULL,
    email      VARCHAR(255)                NOT NULL,
    firstname  VARCHAR(255)                NOT NULL,
    surname    VARCHAR(255)                NOT NULL,
    role       VARCHAR(255)                NOT NULL
        CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'SUPPLY', 'CQ', 'AQ', 'PLANNING', 'PRODUCTION')),
    active     BOOLEAN DEFAULT TRUE        NOT NULL,
    version    BIGINT  DEFAULT 0           NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL
);

