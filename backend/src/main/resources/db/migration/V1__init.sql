-- Flyway V1: estrutura inicial (tenants + users)

-- UUID helper
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================
-- TENANTS
-- =========================
CREATE TABLE IF NOT EXISTS tenants (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(160) NOT NULL,
    slug            VARCHAR(80)  NOT NULL,
    api_key         VARCHAR(128) NOT NULL,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT uq_tenants_slug UNIQUE (slug),
    CONSTRAINT uq_tenants_api_key UNIQUE (api_key)
);

-- =========================
-- USERS
-- =========================
CREATE TABLE IF NOT EXISTS users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Para SUPER_ADMIN: tenant_id deve ser NULL
    -- Para TENANT_ADMIN/EMPLOYEE/CUSTOMER/GUEST: tenant_id deve ser NOT NULL
    tenant_id       UUID NULL,

    email           VARCHAR(254) NULL,
    password_hash   TEXT NULL,

    role            VARCHAR(30) NOT NULL,
    is_active       BOOLEAN     NOT NULL DEFAULT TRUE,

    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE RESTRICT,

    CONSTRAINT ck_users_role CHECK (
        role IN ('SUPER_ADMIN', 'TENANT_ADMIN', 'EMPLOYEE', 'CUSTOMER', 'GUEST')
    ),

    CONSTRAINT ck_users_tenant_scope CHECK (
        (role = 'SUPER_ADMIN' AND tenant_id IS NULL)
        OR
        (role <> 'SUPER_ADMIN' AND tenant_id IS NOT NULL)
    )
);

-- Unicidade de email (case-insensitive) por escopo
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email_global
    ON users (lower(email))
    WHERE email IS NOT NULL AND tenant_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email_per_tenant
    ON users (tenant_id, lower(email))
    WHERE email IS NOT NULL AND tenant_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS ix_users_tenant_id
    ON users (tenant_id);
