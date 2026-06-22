alter table claims
    add column reserved_amount numeric(12, 2) not null default 0.00,
    add column settled_amount numeric(12, 2) not null default 0.00,
    add constraint chk_reserved_amount_positive
        check (reserved_amount >= 0),
    add constraint chk_settled_amount_positive
        check (settled_amount >= 0);

alter table claim_movements
    add column amount numeric(12, 2);

alter table claim_movements
    add constraint chk_claim_movements_amount_positive
        check (amount is null or amount > 0);
