# Insurance Lab Data Model

This document explains the first relational schema for the learning insurance project.

## customers

Stores people or organizations that can own policies.

Important columns:

- `id`: technical database identifier.
- `customer_code`: business identifier for the customer.
- `full_name`: customer display name.

## products

Stores insurance products that can be sold.

Examples:

- Home Insurance
- Auto Insurance
- Travel Insurance

Important columns:

- `id`: technical database identifier.
- `product_code`: business identifier for the product.
- `name`: readable product name.

## coverages

Stores protections that can be included in policies.

Examples:

- Fire damage
- Theft protection
- Legal assistance

Important columns:

- `id`: technical database identifier.
- `coverage_code`: business identifier for the coverage.
- `description`: readable coverage description.
- `insured_amount`: maximum amount covered.

## policies

Stores insurance contracts.

Important columns:

- `id`: technical database identifier.
- `policy_number`: business identifier for the policy.
- `customer_id`: required link to `customers`.
- `product_id`: required link to `products`.
- `start_date`: first day of policy validity.
- `end_date`: last day of policy validity.
- `status`: lifecycle state of the policy.

## policy_coverage

Connects policies to coverages.

A policy can have many coverages, and the same coverage can appear in many policies.

Important columns:

- `policy_id`: link to `policies`.
- `coverage_id`: link to `coverages`.

## premiums

Stores amounts due for policies.

Important columns:

- `id`: technical database identifier.
- `policy_id`: required link to `policies`.
- `amount`: amount due.
- `due_date`: date when the premium is due.

## payments

Stores payment attempts or completed payments.

Important columns:

- `id`: technical database identifier.
- `payment_id`: business identifier for the payment.
- `policy_id`: required link to `policies`.
- `amount`: paid amount.
- `payment_date`: date of payment.
- `status`: payment lifecycle state.

## claims

Stores reported losses against policies.

Important columns:

- `id`: technical database identifier.
- `claim_number`: business identifier for the claim.
- `policy_id`: required link to `policies`.
- `loss_date`: date when the loss happened.
- `claimed_amount`: amount requested.
- `status`: claim lifecycle state.

## claim_movements

Stores claim history events.

Important columns:

- `id`: technical database identifier.
- `claim_id`: required link to `claims`.
- `status`: claim status at this movement.
- `note`: optional explanation.
- `created_at`: technical timestamp when the movement happened.
