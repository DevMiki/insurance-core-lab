-- 1. List all customers.
select id,
       customer_code,
       full_name
from customers
order by full_name;

-- 2. List all products.
select id,
       product_code,
       name
from products
order by product_code;

-- 3. Find active policies with customer and product information.
select p.policy_number,
       c.full_name as customer_name,
       pr.name    as product_name,
       p.start_date,
       p.end_date,
       p.status
from policies p
         join customers c on c.id = p.customer_id
         join products pr on pr.id = p.product_id
where p.status = 'ACTIVE'
order by p.end_date;

-- 4. Find policies expiring in the next 30 days.
select p.policy_number,
       c.full_name as customer_name,
       p.end_date,
       p.status
from policies p
         join customers c on c.id = p.customer_id
where p.end_date between current_date and current_date +
    interval '30 days'
order by p.end_date;

-- 5. List coverages for each policy.
select p.policy_number,
       cv.coverage_code,
       cv.description,
       cv.insured_amount
from policy_coverage pc
         join policies p on p.id = pc.policy_id
         join coverages cv on cv.id = pc.coverage_id
order by p.policy_number, cv.coverage_code;

-- 6. Find premiums due by month.
select date_trunc('month', due_date) as premium_month,
       count(*)                      as premium_count,
       sum(amount)                   as total_amount
from premiums
group by date_trunc('month', due_date)
order by premium_month;

-- 7. Find payments by status.
select status,
       count(*)    as payment_count,
       sum(amount) as total_amount
from payments
group by status
order by status;

-- 8. Find open claims with policy and customer information.
select cl.claim_number,
       p.policy_number,
       c.full_name as customer_name,
       cl.loss_date,
       cl.claimed_amount,
       cl.status
from claims cl
         join policies p on p.id = cl.policy_id
         join customers c on c.id = p.customer_id
where cl.status in ('OPENED', 'UNDER_REVIEW', 'RESERVED')
order by cl.loss_date;

-- 9. Show claim movement history.
select cl.claim_number,
       cm.status,
       cm.note,
       cm.created_at
from claim_movements cm
         join claims cl on cl.id = cm.claim_id
order by cl.claim_number, cm.created_at;
