create table plans (
    id uuid primary key,
    code varchar(50) not null unique,
    name varchar(100) not null,
    stripe_price_id varchar(100),
    amount_cents bigint not null,
    currency varchar(3) not null,
    billing_interval varchar(20) not null,
    active boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

insert into plans (id, code, name, amount_cents, currency, billing_interval, created_at, updated_at)
values
    (gen_random_uuid(), 'basic_monthly', 'Basic monthly', 999, 'usd', 'month', now(), now()),
    (gen_random_uuid(), 'pro_monthly', 'Pro monthly', 1999, 'usd', 'month', now(), now());

create table payment_transactions (
    id uuid primary key,
    user_id uuid not null,
    plan_id uuid not null references plans (id),
    checkout_session_id varchar(200) not null unique,
    checkout_url text not null,
    status varchar(20) not null,
    amount_cents bigint not null,
    currency varchar(3) not null,
    stripe_subscription_id varchar(100),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create index idx_payment_transactions_user on payment_transactions (user_id);
create index idx_payment_transactions_plan on payment_transactions (plan_id);
create index idx_payment_transactions_status on payment_transactions (status);
create index idx_payment_transactions_subscription on payment_transactions (stripe_subscription_id);

create table payment_attempts (
    id uuid primary key,
    transaction_id uuid not null references payment_transactions (id),
    outcome varchar(20) not null,
    detail varchar(200),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create index idx_payment_attempts_transaction on payment_attempts (transaction_id);

create table subscriptions (
    id uuid primary key,
    user_id uuid not null,
    plan_id uuid not null references plans (id),
    transaction_id uuid not null references payment_transactions (id),
    stripe_subscription_id varchar(100) not null unique,
    status varchar(20) not null,
    started_at timestamptz not null,
    cancelled_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create index idx_subscriptions_user on subscriptions (user_id);
create index idx_subscriptions_plan on subscriptions (plan_id);
create index idx_subscriptions_transaction on subscriptions (transaction_id);
create index idx_subscriptions_status on subscriptions (status);
create unique index uq_subscriptions_one_active on subscriptions (user_id) where status = 'ACTIVE';

create table payment_webhook_events (
    id uuid primary key,
    provider varchar(30) not null,
    provider_event_id varchar(100) not null,
    event_type varchar(100) not null,
    received_at timestamptz not null,
    created_at timestamptz not null default now(),
    constraint uq_payment_webhook_events unique (provider, provider_event_id)
);

create table idempotency_keys (
    id uuid primary key,
    user_id uuid not null,
    idempotency_key varchar(100) not null,
    request_hash varchar(64) not null,
    transaction_id uuid references payment_transactions (id),
    created_at timestamptz not null,
    constraint uq_idempotency_keys unique (user_id, idempotency_key)
);

create index idx_idempotency_keys_transaction on idempotency_keys (transaction_id);
