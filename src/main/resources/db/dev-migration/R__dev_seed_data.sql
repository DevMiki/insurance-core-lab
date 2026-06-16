-- Development seed data for the insurance lab.
-- This is fake learning data, not production or proprietary insurance data.
--
-- This repeatable Flyway migration is intended for local development only.

insert into customers (customer_code, full_name)
values
    ('CUST-001', 'Mario Rossi'),
    ('CUST-002', 'Giulia Bianchi'),
    ('CUST-003', 'Luca Verdi')
on conflict (customer_code) do nothing;

insert into products (product_code, name)
values
    ('HOME-BASIC', 'Home Basic'),
    ('HOME-PLUS', 'Home Plus'),
    ('TRAVEL-BASIC', 'Travel Basic')
on conflict (product_code) do nothing;

insert into coverages (code, name, description, base_price, product_id)
values
    (
        'HOME-FIRE',
        'Fire Damage',
        'Fake learning coverage for fire damage.',
        2.50,
        (select id from products where product_code = 'HOME-BASIC')
    ),
    (
        'HOME-THEFT',
        'Theft Protection',
        'Fake learning coverage for theft protection.',
        1.75,
        (select id from products where product_code = 'HOME-BASIC')
    ),
    (
        'HOME-WATER',
        'Water Damage',
        'Fake learning coverage for water damage.',
        1.25,
        (select id from products where product_code = 'HOME-PLUS')
    ),
    (
        'HOME-GLASS',
        'Glass Damage',
        'Fake learning coverage for glass damage.',
        0.80,
        (select id from products where product_code = 'HOME-PLUS')
    ),
    (
        'TRAVEL-MEDICAL',
        'Travel Medical',
        'Fake learning coverage for travel medical expenses.',
        3.00,
        (select id from products where product_code = 'TRAVEL-BASIC')
    ),
    (
        'TRAVEL-BAGGAGE',
        'Travel Baggage',
        'Fake learning coverage for baggage loss.',
        1.10,
        (select id from products where product_code = 'TRAVEL-BASIC')
    )
on conflict (code) do nothing;

insert into policies (policy_number, customer_id, product_id, start_date, end_date, status)
values
    (
        'POL-2026-001',
        (select id from customers where customer_code = 'CUST-001'),
        (select id from products where product_code = 'HOME-BASIC'),
        date '2026-01-01',
        date '2027-01-01',
        'ACTIVE'
    ),
    (
        'POL-2026-002',
        (select id from customers where customer_code = 'CUST-002'),
        (select id from products where product_code = 'TRAVEL-BASIC'),
        date '2026-06-01',
        date '2026-06-15',
        'ACTIVE'
    )
on conflict (policy_number) do nothing;

insert into policy_coverage (policy_id, coverage_id)
values
    (
        (select id from policies where policy_number = 'POL-2026-001'),
        (select id from coverages where code = 'HOME-FIRE')
    ),
    (
        (select id from policies where policy_number = 'POL-2026-001'),
        (select id from coverages where code = 'HOME-THEFT')
    ),
    (
        (select id from policies where policy_number = 'POL-2026-002'),
        (select id from coverages where code = 'TRAVEL-MEDICAL')
    ),
    (
        (select id from policies where policy_number = 'POL-2026-002'),
        (select id from coverages where code = 'TRAVEL-BAGGAGE')
    )
on conflict (policy_id, coverage_id) do nothing;

insert into premiums (policy_id, amount, due_date)
select policy_id, amount, due_date
from (
    values
        (
            (select id from policies where policy_number = 'POL-2026-001'),
            365.00,
            date '2026-01-01'
        ),
        (
            (select id from policies where policy_number = 'POL-2026-002'),
            57.40,
            date '2026-06-01'
        )
) as seed(policy_id, amount, due_date)
where not exists (
    select 1
    from premiums
    where premiums.policy_id = seed.policy_id
      and premiums.due_date = seed.due_date
);

insert into payments (external_reference, policy_id, amount, payment_date, status)
values
    (
        'PAY-2026-001',
        (select id from policies where policy_number = 'POL-2026-001'),
        365.00,
        date '2026-01-02',
        'PAID'
    )
on conflict (external_reference) do nothing;

insert into claims (claim_number, policy_id, loss_date, claimed_amount, status)
values
    (
        'CLM-2026-001',
        (select id from policies where policy_number = 'POL-2026-001'),
        date '2026-03-10',
        450.00,
        'OPEN'
    )
on conflict (claim_number) do nothing;

insert into claim_movements (claim_id, status, note, created_at)
select claim_id, status, note, created_at
from (
    values
        (
            (select id from claims where claim_number = 'CLM-2026-001'),
            'OPEN',
            'Fake learning claim created for local testing.',
            timestamp with time zone '2026-03-10 09:00:00+00'
        )
) as seed(claim_id, status, note, created_at)
where not exists (
    select 1
    from claim_movements
    where claim_movements.claim_id = seed.claim_id
      and claim_movements.status = seed.status
      and claim_movements.created_at = seed.created_at
);
