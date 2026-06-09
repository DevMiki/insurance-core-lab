create table policies
(
    id            bigserial primary key,
    policy_number varchar(50) not null unique,
    customer_id   bigint      not null references customers (id),
    product_id    bigint      not null references products (id),
    start_date    date        not null,
    end_date      date        not null,
    status        varchar(30) not null,
    constraint chk_policies_date_order
        check (end_date > start_date)
);
