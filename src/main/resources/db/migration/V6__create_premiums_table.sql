create table premiums
(
    id        bigserial primary key,
    policy_id bigint         not null references policies (id),
    amount    numeric(12, 2) not null,
    due_date  date           not null,
    constraint chk_premiums_amount_positive
        check ( amount > 0)
);

create index idx_premiums_policy_id on premiums(policy_id);
create index idx_premiums_due_date on premiums(due_date);