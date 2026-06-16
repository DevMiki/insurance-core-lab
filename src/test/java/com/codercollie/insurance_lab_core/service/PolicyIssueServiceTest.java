package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.domain.PolicyStatus;
import com.codercollie.insurance_lab_core.dto.policy.PolicyResponse;
import com.codercollie.insurance_lab_core.exception.InvalidPolicyIssueRequestException;
import com.codercollie.insurance_lab_core.mapper.PolicyMapper;
import com.codercollie.insurance_lab_core.persistence.entity.CoverageEntity;
import com.codercollie.insurance_lab_core.persistence.entity.PolicyEntity;
import com.codercollie.insurance_lab_core.persistence.entity.PremiumEntity;
import com.codercollie.insurance_lab_core.persistence.entity.ProductEntity;
import com.codercollie.insurance_lab_core.persistence.entity.QuoteEntity;
import com.codercollie.insurance_lab_core.repository.PolicyRepository;
import com.codercollie.insurance_lab_core.repository.PremiumRepository;
import com.codercollie.insurance_lab_core.repository.QuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyIssueServiceTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PremiumRepository premiumRepository;

    private PolicyIssueService policyIssueService;

    @BeforeEach
    void setUp() {
        policyIssueService = new PolicyIssueService(
                quoteRepository,
                policyRepository,
                premiumRepository,
                new PolicyMapper()
        );
    }

    @Test
    void issuesPolicyFromQuote() {
        CoverageEntity fire = coverageWithIdAndProductId(10L, "FIRE", new BigDecimal("100.00"), 1L);
        CoverageEntity theft = coverageWithIdAndProductId(11L, "THEFT", new BigDecimal("50.00"), 1L);
        QuoteEntity quote = new QuoteEntity(
                1L,
                2L,
                Set.of(fire, theft),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 11),
                new BigDecimal("1500.00"),
                new BigDecimal("330.00"),
                new BigDecimal("1830.00"),
                Instant.parse("2026-01-01T10:00:00Z")
        );
        ReflectionTestUtils.setField(quote, "id", 99L);

        when(quoteRepository.findById(99L)).thenReturn(Optional.of(quote));
        when(policyRepository.existsByQuoteId(99L)).thenReturn(false);

        when(policyRepository.save(ArgumentMatchers.any(PolicyEntity.class)))
                .thenAnswer(invocation -> {
                    PolicyEntity policy = invocation.getArgument(0);
                    ReflectionTestUtils.setField(policy, "id", 123L);
                    return policy;
                });

        PolicyResponse response = policyIssueService.issueQuote(99L);

        ArgumentCaptor<PremiumEntity> premiumCaptor = ArgumentCaptor.forClass(PremiumEntity.class);
        verify(premiumRepository).save(premiumCaptor.capture());
        PremiumEntity premium = premiumCaptor.getValue();
        assertEquals(new BigDecimal("1830.00"), premium.getAmount());
        assertEquals(LocalDate.of(2026, 1, 1), premium.getDueDate());

        assertEquals(123L, response.id());
        assertEquals("POL-" + LocalDate.now().getYear() + "-000099", response.policyNumber());
        assertEquals(99L, response.quoteId());
        assertEquals(2L, response.customerId());
        assertEquals(1L, response.productId());
        assertEquals(List.of(10L, 11L), response.coverageIds());
        assertEquals(LocalDate.of(2026, 1, 1), response.startDate());
        assertEquals(LocalDate.of(2026, 1, 11), response.endDate());
        assertEquals(PolicyStatus.ISSUED, response.status());
    }

    @Test
    void rejectsQuoteThatWasAlreadyIssued() {
        QuoteEntity quote = new QuoteEntity(
                1L,
                2L,
                Set.of(coverageWithIdAndProductId(10L, "FIRE", new BigDecimal("100.00"), 1L)),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 11),
                new BigDecimal("1000.00"),
                new BigDecimal("220.00"),
                new BigDecimal("1220.00"),
                Instant.parse("2026-01-01T10:00:00Z")
        );
        ReflectionTestUtils.setField(quote, "id", 99L);

        when(quoteRepository.findById(99L)).thenReturn(Optional.of(quote));
        when(policyRepository.existsByQuoteId(99L)).thenReturn(true);

        InvalidPolicyIssueRequestException exception = assertThrows(
                InvalidPolicyIssueRequestException.class,
                () -> policyIssueService.issueQuote(99L)
        );

        assertEquals("quote has already been issued", exception.getMessage());
        verify(policyRepository, never()).save(ArgumentMatchers.any(PolicyEntity.class));
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
