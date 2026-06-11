package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.domain.quote.CoveragePrice;
import com.codercollie.insurance_lab_core.domain.quote.PremiumCalculationResult;
import com.codercollie.insurance_lab_core.domain.quote.PremiumCalculator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
public class SimplePremiumCalculator implements PremiumCalculator {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.22");
    private static final int MONEY_SCALE = 2;

    @Override
    public PremiumCalculationResult calculate(List<CoveragePrice> coveragePrices, LocalDate startDate, LocalDate endDate) {

        if (coveragePrices == null || coveragePrices.isEmpty()) {
            throw new IllegalArgumentException("at least one coverage is required");
        }
        Objects.requireNonNull(startDate, "startDate must not be null");
        Objects.requireNonNull(endDate, "endDate must not be null");

        final long durationDays = ChronoUnit.DAYS.between(startDate, endDate);
        if (durationDays <= 0) {
            throw new IllegalArgumentException("endDate must be after startDate");
        }

        final BigDecimal totalCoveragesDailyPrices = coveragePrices
                .stream()
                .map(CoveragePrice::basePrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal netPremium = toMoneyScale(
                totalCoveragesDailyPrices.multiply(
                        BigDecimal.valueOf(durationDays)
                ));

        final BigDecimal taxAmount = toMoneyScale(
                netPremium.multiply(TAX_RATE)
        );

        final BigDecimal totalAmount = toMoneyScale(
                netPremium.add(taxAmount)
        );

        return new PremiumCalculationResult(
                netPremium,
                taxAmount,
                totalAmount
        );
    }

    private BigDecimal toMoneyScale(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
