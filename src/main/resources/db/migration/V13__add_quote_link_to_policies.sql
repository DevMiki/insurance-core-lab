alter table policies
    add column quote_id bigint null;

alter table policies
    add constraint fk_policies_quote
        foreign key (quote_id) references quotes (id);

create unique index ux_policies_quote_id
    on policies (quote_id)
    where quote_id is not null;
