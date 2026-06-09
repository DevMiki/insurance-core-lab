create table claim_movements
(
    id         bigserial primary key,
    claim_id   bigint                   not null references claims (id),
    status     varchar(30)              not null,
    note       varchar(500),
    created_at timestamp with time zone not null
);

create index idx_claim_movements_claim_id on
    claim_movements (claim_id);
create index idx_claim_movements_created_at on
    claim_movements (created_at);