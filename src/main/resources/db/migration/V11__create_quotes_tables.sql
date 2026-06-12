create table quotes
(
    id           bigserial primary key,
    product_id   bigint         not null references products (id),
    start_date   date           not null,
    end_date     date           not null,
    net_premium  numeric(12, 2) not null,
    tax_amount   numeric(12, 2) not null,
    total_amount numeric(12, 2) not null,
    created_at   timestamptz    not null,
    constraint chk_quotes_dates_valid check (end_date > start_date),
    constraint chk_quotes_net_premium_positive check (net_premium > 0),
    constraint chk_quotes_tax_amount_not_negative check (tax_amount >= 0),
    constraint chk_quotes_total_amount_positive check (total_amount > 0)
);

create table quote_coverages
(
    quote_id    bigint not null references quotes (id) on delete cascade,
    coverage_id bigint not null references coverages (id),
    primary key (quote_id, coverage_id)
);

create index idx_quotes_product_id on quotes (product_id);
create index idx_quotes_start_date on quotes (start_date);
create index idx_quote_coverages_coverage_id on quote_coverages (coverage_id);
