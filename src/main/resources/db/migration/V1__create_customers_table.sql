create table customers
(
    id            bigserial primary key,
    customer_code varchar(50)  not null unique,
    fullname      varchar(200) not null
);
