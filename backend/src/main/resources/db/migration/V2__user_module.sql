create table users (
    id uuid primary key,
    email varchar(320) not null,
    password_hash varchar(100) not null,
    role varchar(20) not null,
    email_verified boolean not null default false,
    failed_login_attempts integer not null default 0,
    locked_until timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create unique index uq_users_email_lower on users (lower(email));

create table user_profiles (
    id uuid primary key,
    user_id uuid not null unique references users (id),
    full_name varchar(200) not null,
    phone_number varchar(20),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create table email_verification_tokens (
    id uuid primary key,
    user_id uuid not null references users (id),
    token_hash varchar(64) not null unique,
    expires_at timestamptz not null,
    used_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create index idx_email_verification_tokens_user on email_verification_tokens (user_id);

create table password_reset_tokens (
    id uuid primary key,
    user_id uuid not null references users (id),
    token_hash varchar(64) not null unique,
    expires_at timestamptz not null,
    used_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create index idx_password_reset_tokens_user on password_reset_tokens (user_id);

create table refresh_tokens (
    id uuid primary key,
    user_id uuid not null references users (id),
    family_id uuid not null,
    token_hash varchar(64) not null unique,
    expires_at timestamptz not null,
    revoked_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create index idx_refresh_tokens_user on refresh_tokens (user_id);
create index idx_refresh_tokens_family on refresh_tokens (family_id);
