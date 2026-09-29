create table verification_sessions (
    id uuid primary key,
    user_id uuid not null,
    provider varchar(30) not null,
    provider_ref varchar(100) not null,
    status varchar(20) not null,
    expires_at timestamptz not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create unique index uq_verification_sessions_provider_ref on verification_sessions (provider, provider_ref);
create index idx_verification_sessions_user on verification_sessions (user_id);
create index idx_verification_sessions_status on verification_sessions (status);
create unique index uq_verification_sessions_one_active on verification_sessions (user_id)
    where status in ('PENDING', 'IN_PROGRESS');

create table verification_results (
    id uuid primary key,
    session_id uuid not null unique references verification_sessions (id),
    outcome varchar(20) not null,
    reason varchar(200),
    decided_at timestamptz not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create table verification_webhook_events (
    id uuid primary key,
    provider varchar(30) not null,
    provider_event_id varchar(100) not null,
    event_type varchar(100) not null,
    received_at timestamptz not null,
    created_at timestamptz not null default now(),
    constraint uq_verification_webhook_events unique (provider, provider_event_id)
);
