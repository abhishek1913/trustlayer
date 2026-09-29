create table notification_events (
    id uuid primary key,
    event_id uuid not null unique,
    event_type varchar(50) not null,
    user_id uuid not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create index idx_notification_events_user on notification_events (user_id);

create table notification_deliveries (
    id uuid primary key,
    notification_event_id uuid not null references notification_events (id),
    user_id uuid not null,
    event_type varchar(50) not null,
    channel varchar(20) not null,
    status varchar(20) not null,
    attempts integer not null default 0,
    last_error varchar(200),
    sent_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    version bigint not null default 0
);

create index idx_notification_deliveries_event on notification_deliveries (notification_event_id);
create index idx_notification_deliveries_user on notification_deliveries (user_id);
create index idx_notification_deliveries_status on notification_deliveries (status);
create index idx_notification_deliveries_channel on notification_deliveries (channel);
