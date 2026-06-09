create table payments
(
    id           bigserial primary key,
    payment_id   varchar(50)    not null unique,
    policy_id    bigint         not null references policies (id),
    amount       numeric(12, 2) not null,
    payment_date date           not null,
    status       varchar(30)    not null,
    constraint chk_payments_amount_positive check (amount > 0)
);

create index idx_payments_policy_id on payments(policy_id);
create index idx_payments_payment_date on payments (payment_date);
create index idx_payments_status on payments (status);

