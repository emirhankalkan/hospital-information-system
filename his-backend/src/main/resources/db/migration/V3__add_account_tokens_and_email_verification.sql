alter table if exists users
    add column if not exists email_verified boolean not null default true;

create table if not exists refresh_tokens (
    id bigserial primary key,
    user_id bigint not null,
    token_hash varchar(64) not null unique,
    expires_at timestamp not null,
    revoked boolean not null default false,
    created_at timestamp
);

create index if not exists idx_refresh_tokens_user_id
    on refresh_tokens(user_id);

create table if not exists account_tokens (
    id bigserial primary key,
    user_id bigint not null,
    token_type varchar(30) not null,
    token_hash varchar(64) not null unique,
    expires_at timestamp not null,
    used_at timestamp,
    created_at timestamp
);

create index if not exists idx_account_tokens_user_type
    on account_tokens(user_id, token_type);
