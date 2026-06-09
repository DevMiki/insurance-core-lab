create table products (
    id bigserial primary key,
    product_code varchar(50) not null unique,
    name varchar(200) not null
)