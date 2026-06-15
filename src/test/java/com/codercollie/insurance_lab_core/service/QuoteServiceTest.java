package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.domain.quote.PremiumCalculator;
import com.codercollie.insurance_lab_core.dto.quote.CreateQuoteRequest;
import com.codercollie.insurance_lab_core.dto.quote.QuoteResponse;
import com.codercollie.insurance_lab_core.exception.InvalidQuoteRequestException;
import com.codercollie.insurance_lab_core.exception.ResourceNotFoundException;
import com.codercollie.insurance_lab_core.mapper.QuoteMapper;
import com.codercollie.insurance_lab_core.persistence.entity.CoverageEntity;
import com.codercollie.insurance_lab_core.persistence.entity.ProductEntity;
import com.codercollie.insurance_lab_core.persistence.entity.QuoteEntity;
import com.codercollie.insurance_lab_core.repository.CoverageRepository;
import com.codercollie.insurance_lab_core.repository.CustomerRepository;
import com.codercollie.insurance_lab_core.repository.ProductRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private CoverageRepository coverageRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    private QuoteService quoteService;

    @BeforeEach
    void setUp() {
        PremiumCalculator premiumCalculator = new SimplePremiumCalculator();
        quoteService = new QuoteService(
                quoteRepository,
                coverageRepository,
                productRepository,
                customerRepository,
                premiumCalculator,
                new QuoteMapper()
        );
    }

    @Test
    void createsQuoteWithCalculatedPremium() {
        CoverageEntity fire = coverageWithIdAndProductId(10L, "FIRE", new BigDecimal("100.00"), 1L);
        CoverageEntity theft = coverageWithIdAndProductId(11L, "THEFT", new BigDecimal("50.00"), 1L);

        when(customerRepository.existsById(2L)).thenReturn(true);
        when(productRepository.existsById(1L)).thenReturn(true);
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
                        2L,
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
        CoverageEntity fire = coverageWithIdAndProductId(10L, "FIRE", new BigDecimal("100.00"), 1L);

        when(customerRepository.existsById(2L)).thenReturn(true);
        when(productRepository.existsById(1L)).thenReturn(true);
        when(coverageRepository.findAllById(List.of(10L, 999L)))
                .thenReturn(List.of(fire));

        InvalidQuoteRequestException exception = assertThrows(
                InvalidQuoteRequestException.class,
                () -> quoteService.createQuote(
                        new CreateQuoteRequest(
                                1L,
                                2L,
                                List.of(10L, 999L),
                                LocalDate.of(2026, 1, 1),
                                LocalDate.of(2026, 1, 11)
                        )
                )
        );

        assertEquals("one or more coverages were not found", exception.getMessage());
    }

    @Test
    void rejectsDuplicateCoverageIds() {
        InvalidQuoteRequestException exception = assertThrows(
                InvalidQuoteRequestException.class,
                () -> quoteService.createQuote(
                        new CreateQuoteRequest(
                                1L,
                                2L,
                                List.of(10L, 10L),
                                LocalDate.of(2026, 1, 1),
                                LocalDate.of(2026, 1, 11)
                        )
                )
        );

        assertEquals("coverageIds must not contain duplicates", exception.getMessage());
        verifyNoInteractions(coverageRepository, quoteRepository);
    }

    @Test
    void rejectsQuoteWhenProductDoesNotExist() {
        when(customerRepository.existsById(2L)).thenReturn(true);
        when(productRepository.existsById(999L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> quoteService.createQuote(
                        new CreateQuoteRequest(
                                999L,
                                2L,
                                List.of(10L),
                                LocalDate.of(2026, 1, 1),
                                LocalDate.of(2026, 1, 11)
                        )
                )
        );

        assertEquals("product not found", exception.getMessage());
        verifyNoInteractions(coverageRepository, quoteRepository);
    }

    @Test
    void rejectsQuoteWhenCustomerDoesNotExist() {
        when(customerRepository.existsById(999L)).thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> quoteService.createQuote(
                        new CreateQuoteRequest(
                                1L,
                                999L,
                                List.of(10L),
                                LocalDate.of(2026, 1, 1),
                                LocalDate.of(2026, 1, 11)
                        )
                )
        );

        verifyNoInteractions(productRepository, coverageRepository, quoteRepository);
        assertEquals("customer not found", exception.getMessage());
    }

    @Test
    void rejectsCoverageFromDifferentProduct() {
        CoverageEntity fire = coverageWithIdAndProductId(10L, "FIRE", new BigDecimal("100.00"), 1L);
        CoverageEntity theft = coverageWithIdAndProductId(11L, "THEFT", new BigDecimal("50.00"), 2L);

        when(customerRepository.existsById(2L)).thenReturn(true);
        when(productRepository.existsById(1L)).thenReturn(true);
        when(coverageRepository.findAllById(List.of(10L, 11L))).thenReturn(List.of(fire, theft));

        InvalidQuoteRequestException exception = assertThrows(
                InvalidQuoteRequestException.class,
                () -> quoteService.createQuote(
                        new CreateQuoteRequest(
                                1L,
                                2L,
                                List.of(10L, 11L),
                                LocalDate.of(2026, 1, 1),
                                LocalDate.of(2026, 1, 11)
                        )
                )
        );

        assertEquals(
                "one or more coverages do not belong to the selected product",
                exception.getMessage()
        );
    }

    @Test
    void getsQuoteById() {
        QuoteEntity quote = new QuoteEntity(
                1L,
                List.of(10L, 11L),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 11),
                new BigDecimal("1500.00"),
                new BigDecimal("330.00"),
                new BigDecimal("1830.00"),
                Instant.parse("2026-01-01T10:00:00Z")
        );
        ReflectionTestUtils.setField(quote, "id", 99L);

        when(quoteRepository.findById(99L)).thenReturn(Optional.of(quote));

        QuoteResponse response = quoteService.getQuoteById(99L);

        assertEquals(99L, response.id());
        assertEquals(1L, response.productId());
        assertEquals(List.of(10L, 11L), response.coverageIds());
        assertEquals(new BigDecimal("1830.00"), response.totalAmount());
    }

    @Test
    void rejectsMissingQuoteById() {
        when(quoteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> quoteService.getQuoteById(999L)
        );
    }

    private CoverageEntity coverageWithIdAndProductId(Long id, String code, BigDecimal basePrice, Long productId) {
        CoverageEntity coverage = new CoverageEntity(
                code,
                code + " coverage",
                "Test coverage",
                basePrice
        );
        ReflectionTestUtils.setField(coverage, "id", id);

        ProductEntity product = new ProductEntity("PRODUCT-" + productId, "Product " + productId);
        ReflectionTestUtils.setField(product, "id", productId);
        product.addCoverage(coverage);

        return coverage;
    }
}
