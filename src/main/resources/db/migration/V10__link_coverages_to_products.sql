alter table coverages
    rename column coverage_code to code;

alter table coverages
    rename column insured_amount to base_price;

alter table coverages
    rename constraint chk_coverages_insured_amount_positive to chk_coverages_base_price_positive;

alter table coverages
    add column name varchar(200);

update coverages
set name = description
where name is null;

alter table coverages
    alter column name set not null;

alter table coverages
    alter column description type varchar(500);

alter table coverages
    add column product_id bigint references products (id);

insert into products (product_code, name)
select 'LEGACY', 'Legacy Product'
where exists (
    select 1
    from coverages
    where product_id is null
)
on conflict (product_code) do nothing;

update coverages
set product_id = (
    select id
    from products
    where product_code = 'LEGACY'
)
where product_id is null;

alter table coverages
    alter column product_id set not null;

create index idx_coverages_product_id on coverages (product_id);
