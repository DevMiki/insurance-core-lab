package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.domain.PolicyStatus;
import com.codercollie.insurance_lab_core.dto.policy.PolicyResponse;
import com.codercollie.insurance_lab_core.mapper.PolicyMapper;
import com.codercollie.insurance_lab_core.persistence.entity.CoverageEntity;
import com.codercollie.insurance_lab_core.persistence.entity.PolicyEntity;
import com.codercollie.insurance_lab_core.persistence.entity.ProductEntity;
import com.codercollie.insurance_lab_core.persistence.entity.QuoteEntity;
import com.codercollie.insurance_lab_core.repository.CoverageRepository;
import com.codercollie.insurance_lab_core.repository.PolicyRepository;
import com.codercollie.insurance_lab_core.repository.QuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PolicyIssueServiceTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private CoverageRepository coverageRepository;

    private PolicyIssueService policyIssueService;

    @BeforeEach
    void setUp() {
        policyIssueService = new PolicyIssueService(
                quoteRepository,
                policyRepository,
                coverageRepository,
                new PolicyMapper()
        );
    }

    @Test
    void issuesPolicyFromQuote() {
        QuoteEntity quote = new QuoteEntity(
                1L,
                2L,
                List.of(10L, 11L),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 11),
                new BigDecimal("1500.00"),
                new BigDecimal("330.00"),
                new BigDecimal("1830.00"),
                Instant.parse("2026-01-01T10:00:00Z")
        );
        ReflectionTestUtils.setField(quote, "id", 99L);

        CoverageEntity fire = coverageWithIdAndProductId(10L, "FIRE", new BigDecimal("100.00"), 1L);
        CoverageEntity theft = coverageWithIdAndProductId(11L, "THEFT", new BigDecimal("50.00"), 1L);

        when(quoteRepository.findById(99L)).thenReturn(Optional.of(quote));
        when(policyRepository.existsByQuoteId(99L)).thenReturn(false);
        when(coverageRepository.findAllById(List.of(10L, 11L))).thenReturn(List.of(fire, theft));

        when(policyRepository.save(ArgumentMatchers.any(PolicyEntity.class)))
                .thenAnswer(invocation -> {
                    PolicyEntity policy = invocation.getArgument(0);
                    ReflectionTestUtils.setField(policy, "id", 123L);
                    return policy;
                });

        PolicyResponse response = policyIssueService.issueQuote(99L);

        assertEquals(123L, response.id());
        assertEquals(99L, response.quoteId());
        assertEquals(2L, response.customerId());
        assertEquals(1L, response.productId());
        assertEquals(List.of(10L, 11L), response.coverageIds());
        assertEquals(LocalDate.of(2026, 1, 1), response.startDate());
        assertEquals(LocalDate.of(2026, 1, 11), response.endDate());
        assertEquals(PolicyStatus.ISSUED, response.status());
    }

    private CoverageEntity coverageWithIdAndProductId(Long coverageId, String code, BigDecimal basePrice, Long productId) {
        CoverageEntity coverage = new CoverageEntity(
                code,
                code + " coverage",
                "Test coverage",
                basePrice
        );
        ReflectionTestUtils.setField(coverage, "id", coverageId);

        ProductEntity product = new ProductEntity("PRODUCT-" + productId, "Product " + productId);
        ReflectionTestUtils.setField(product, "id", productId);
        product.addCoverage(coverage);

        return coverage;

    }

}
