create table customers
(
    id            bigserial primary key,
    customer_code varchar(50)  not null unique,
    full_name      varchar(200) not null
);
