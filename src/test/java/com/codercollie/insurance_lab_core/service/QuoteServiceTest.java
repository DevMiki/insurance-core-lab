package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.domain.quote.PremiumCalculator;
import com.codercollie.insurance_lab_core.dto.quote.CreateQuoteRequest;
import com.codercollie.insurance_lab_core.dto.quote.QuoteResponse;
import com.codercollie.insurance_lab_core.mapper.QuoteMapper;
import com.codercollie.insurance_lab_core.persistence.entity.CoverageEntity;
import com.codercollie.insurance_lab_core.persistence.entity.QuoteEntity;
import com.codercollie.insurance_lab_core.repository.CoverageRepository;
import com.codercollie.insurance_lab_core.repository.QuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private CoverageRepository coverageRepository;

    private QuoteService quoteService;

    @BeforeEach
    void setUp() {
        PremiumCalculator premiumCalculator = new SimplePremiumCalculator();
        quoteService = new QuoteService(
                quoteRepository,
                coverageRepository,
                premiumCalculator,
                new QuoteMapper()
        );
    }

    @Test
    void createsQuoteWithCalculatedPremium() {
        CoverageEntity fire = coverageWithId(10L, "FIRE", new BigDecimal("100.00"));
        CoverageEntity theft = coverageWithId(11L, "THEFT", new BigDecimal("50.00"));

        when(coverageRepository.findAllById(List.of(10L, 11L))).thenReturn(List.of(fire, theft));

        when(quoteRepository.save(ArgumentMatchers.any(QuoteEntity.class)))
                .thenAnswer(invocation -> {
                    QuoteEntity quote = invocation.getArgument(0);
                    ReflectionTestUtils.setField(quote, "id", 99L);
                    return quote;
                });

        QuoteResponse response = quoteService.createQuote(
                new CreateQuoteRequest(
                        1L,
                        List.of(10L, 11L),
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 11)
                )
        );

        assertEquals(99L, response.id());
        assertEquals(1L, response.productId());
        assertEquals(List.of(10L, 11L), response.coverageIds());
        assertEquals(new BigDecimal("1500.00"), response.netPremium());
        assertEquals(new BigDecimal("330.00"), response.taxAmount());
        assertEquals(new BigDecimal("1830.00"), response.totalAmount());
    }

    @Test
    void rejectsQuoteWhenAnyCoverageIsMissing() {
        CoverageEntity fire = coverageWithId(10L, "FIRE", new BigDecimal("100.00"));

        when(coverageRepository.findAllById(List.of(10L, 999L)))
                .thenReturn(List.of(fire));

        assertThrows(
                IllegalArgumentException.class,
                () -> quoteService.createQuote(
                        new CreateQuoteRequest(
                                1L,
                                List.of(10L, 999L),
                                LocalDate.of(2026, 1, 1),
                                LocalDate.of(2026, 1, 11)
                        )
                )
        );
    }

    private CoverageEntity coverageWithId(Long id, String code, BigDecimal basePrice) {
        CoverageEntity coverage = new CoverageEntity(
                code,
                code + " coverage",
                "Test coverage",
                basePrice
        );
        ReflectionTestUtils.setField(coverage, "id", id);
        return coverage;
    }
}
