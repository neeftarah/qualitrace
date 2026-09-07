CREATE EXTENSION IF NOT EXISTS plpgsql;

DO $$
    DECLARE
        db_name text := current_database();
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'qualitrace_app') THEN
            CREATE ROLE qualitrace_app WITH LOGIN PASSWORD 'app-secret';
        END IF;

        EXECUTE format('GRANT CONNECT ON DATABASE %I TO qualitrace_app', db_name);
    END $$;

GRANT USAGE ON SCHEMA public TO qualitrace_app;
GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA public TO qualitrace_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO qualitrace_app;

REVOKE UPDATE, DELETE ON audit_trail FROM qualitrace_app;

-- pas de "FOR ROLE root" : sans clause FOR ROLE, ça s'applique au rôle courant
-- (celui qui exécute la migration, quel qu'il soit selon l'environnement)
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE ON TABLES TO qualitrace_app;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO qualitrace_app;