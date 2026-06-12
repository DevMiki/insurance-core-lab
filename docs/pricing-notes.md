# Pricing Notes

This project uses fake pricing logic for learning purposes.

It is not real insurance pricing and must not be treated as actuarial, regulatory, underwriting, or production pricing logic.

## Current Learning Formula

The quote premium is calculated with this simple deterministic formula:

```text
durationDays = days between startDate and endDate
netPremium = sum(selected coverage base prices) * durationDays
taxAmount = netPremium * 22%
totalAmount = netPremium + taxAmount
```

The `endDate` is treated as exclusive.

Example:

```text
startDate = 2026-01-01
endDate = 2026-01-11
durationDays = 10
```

## Money Rules

Money values use `BigDecimal`.

Calculated money values are rounded to 2 decimal places with `RoundingMode.HALF_UP`.

## Design Notes

The pricing rule is isolated behind `PremiumCalculator`.

`QuoteService` orchestrates quote creation, but it does not own the pricing formula.

This keeps business rules easier to test, replace, and discuss.
