package com.codercollie.insurance_lab_core.domain.quote;

import java.time.LocalDate;
import java.util.List;

public interface PremiumCalculator {

    PremiumCalculationResult calculate(
            List<CoveragePrice> coveragePrices,
            LocalDate startDate,
            LocalDate endDate
    );
}
