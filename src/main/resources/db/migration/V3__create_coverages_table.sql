create table coverages
(
    id             bigserial primary key,
    coverage_code  varchar(50)    not null unique,
    description    varchar(200)   not null,
    insured_amount numeric(12, 2) not null,
    constraint chk_coverages_insured_amount_positive
        check (insured_amount > 0)
);
