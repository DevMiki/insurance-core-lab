package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.domain.quote.CoveragePrice;
import com.codercollie.insurance_lab_core.domain.quote.PremiumCalculationResult;
import com.codercollie.insurance_lab_core.domain.quote.PremiumCalculator;
import com.codercollie.insurance_lab_core.dto.quote.CreateQuoteRequest;
import com.codercollie.insurance_lab_core.dto.quote.QuoteResponse;
import com.codercollie.insurance_lab_core.exception.InvalidQuoteRequestException;
import com.codercollie.insurance_lab_core.exception.ResourceNotFoundException;
import com.codercollie.insurance_lab_core.mapper.QuoteMapper;
import com.codercollie.insurance_lab_core.persistence.entity.CoverageEntity;
import com.codercollie.insurance_lab_core.persistence.entity.QuoteEntity;
import com.codercollie.insurance_lab_core.repository.CoverageRepository;
import com.codercollie.insurance_lab_core.repository.CustomerRepository;
import com.codercollie.insurance_lab_core.repository.ProductRepository;
import com.codercollie.insurance_lab_core.repository.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@Transactional
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final CoverageRepository coverageRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final PremiumCalculator premiumCalculator;
    private final QuoteMapper quoteMapper;

    public QuoteService(
            QuoteRepository quoteRepository,
            CoverageRepository coverageRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            PremiumCalculator premiumCalculator,
            QuoteMapper quoteMapper
    ) {
        this.quoteRepository = quoteRepository;
        this.coverageRepository = coverageRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.premiumCalculator = premiumCalculator;
        this.quoteMapper = quoteMapper;
    }

    @Transactional(readOnly = true)
    public QuoteResponse getQuoteById(Long id) {
        QuoteEntity quote = quoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("quote not found"));

        return quoteMapper.toResponse(quote);
    }

    public QuoteResponse createQuote(CreateQuoteRequest request) {
        validateQuoteRequest(request);

        List<CoverageEntity> quoteCoverages = findQuoteCoverages(request);
        PremiumCalculationResult premiumCalculationResult = calculatePremium(request, quoteCoverages);
        QuoteEntity quoteEntity = toQuoteEntity(request, quoteCoverages, premiumCalculationResult);

        QuoteEntity savedQuote = quoteRepository.save(quoteEntity);
        return quoteMapper.toResponse(savedQuote);
    }

    private void validateQuoteRequest(CreateQuoteRequest request) {
        rejectDuplicateCoverageIds(request);
        ensureCustomerExists(request.customerId());
        ensureProductExists(request.productId());
    }

    private void rejectDuplicateCoverageIds(CreateQuoteRequest request) {
        if (new HashSet<>(request.coverageIds()).size() != request.coverageIds().size()) {
            throw new InvalidQuoteRequestException("coverageIds must not contain duplicates");
        }
    }

    private void ensureCustomerExists(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("customer not found");
        }
    }

    private void ensureProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("product not found");
        }
    }

    private List<CoverageEntity> findQuoteCoverages(CreateQuoteRequest request) {
        List<CoverageEntity> quoteCoverages = coverageRepository.findAllById(request.coverageIds());

        ensureAllCoveragesWereFound(request, quoteCoverages);
        ensureCoveragesBelongToProduct(request.productId(), quoteCoverages);

        return quoteCoverages;
    }

    private void ensureAllCoveragesWereFound(CreateQuoteRequest request, List<CoverageEntity> quoteCoverages) {
        if (quoteCoverages.size() != request.coverageIds().size()) {
            throw new InvalidQuoteRequestException("one or more coverages were not found");
        }
    }

    private void ensureCoveragesBelongToProduct(Long productId, List<CoverageEntity> quoteCoverages) {
        boolean hasCoverageFromAnotherProduct = quoteCoverages.stream()
                .anyMatch(coverage -> !coverage.getProductId().equals(productId));

        if (hasCoverageFromAnotherProduct) {
            throw new InvalidQuoteRequestException("one or more coverages do not belong to the selected product");
        }
    }

    private PremiumCalculationResult calculatePremium(CreateQuoteRequest request, List<CoverageEntity> quoteCoverages) {
        return premiumCalculator.calculate(
                toCoveragePrices(quoteCoverages),
                request.startDate(),
                request.endDate()
        );
    }

    private List<CoveragePrice> toCoveragePrices(List<CoverageEntity> quoteCoverages) {
        return quoteCoverages.stream()
                .map(coverage -> new CoveragePrice(
                        coverage.getCode(),
                        coverage.getBasePrice()
                ))
                .toList();
    }

    private QuoteEntity toQuoteEntity(
            CreateQuoteRequest request,
            List<CoverageEntity> quoteCoverages,
            PremiumCalculationResult premiumCalculationResult
    ) {
        return new QuoteEntity(
                request.productId(),
                request.customerId(),
                new LinkedHashSet<>(quoteCoverages),
                request.startDate(),
                request.endDate(),
                premiumCalculationResult.netPremium(),
                premiumCalculationResult.taxAmount(),
                premiumCalculationResult.totalAmount(),
                Instant.now()
        );
    }
}
