create table access_grants (
    id uuid primary key,
    user_id uuid not null unique,
    email_verified boolean not null default false,
    identity_verified boolean not null default false,
    payment_succeeded boolean not null default false,
    subscription_active boolean not null default false,
    state varchar(20) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create index idx_access_grants_state on access_grants (state);
