create table claims
(
    id             bigserial primary key,
    claim_number   varchar(50)    not null unique,
    policy_id      bigint         not null references policies (id),
    loss_date      date           not null,
    claimed_amount numeric(12, 2) not null,
    status         varchar(30)    not null,
    constraint chk_claims_claimed_amount_positive
        check (claimed_amount > 0)
);

create index idx_claims_policy_id on claims (policy_id);
create index idx_claims_loss_date on claims (loss_date);
create index idx_claims_status on claims (status);