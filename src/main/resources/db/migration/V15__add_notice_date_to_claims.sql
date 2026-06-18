alter table claims
    add column notice_date date;

update claims
set notice_date = loss_date
where notice_date is null;

update claims
set status = 'OPENED'
where status = 'OPEN';

alter table claims
    alter column notice_date set not null;