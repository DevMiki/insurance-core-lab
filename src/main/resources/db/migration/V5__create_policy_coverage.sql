create table policy_coverage
(
    policy_id   bigint not null references policies (id),
    coverage_id bigint not null references coverages (id),
    primary key (policy_id, coverage_id)
);
