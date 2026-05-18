alter table users
    add column if not exists full_name varchar(100);

update users
set full_name = username
where full_name is null;

alter table users
    alter column full_name set not null;

alter table patients
    alter column tc_no drop not null;
