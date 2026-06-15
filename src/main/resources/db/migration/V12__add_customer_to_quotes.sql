alter table quotes
    add column customer_id bigint;

update quotes
set customer_id = (
    select id
    from customers
    order by id
    limit 1
)
where customer_id is null;

alter table quotes
    alter column customer_id set not null;

alter table quotes
    add constraint fk_quotes_customer
        foreign key (customer_id) references customers (id);

create index idx_quotes_customer_id on quotes (customer_id);
