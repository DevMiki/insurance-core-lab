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

    public QuoteResponse createQuote(CreateQuoteRequest quoteRequest) {
        if (new HashSet<>(quoteRequest.coverageIds()).size() != quoteRequest.coverageIds().size()) {
            throw new InvalidQuoteRequestException("coverageIds must not contain duplicates");
        }

        if (!customerRepository.existsById(quoteRequest.customerId())) {
            throw new ResourceNotFoundException("customer not found");
        }

        if (!productRepository.existsById(quoteRequest.productId())) {
            throw new ResourceNotFoundException("product not found");
        }

        List<CoverageEntity> quoteCoverages = coverageRepository.findAllById(quoteRequest.coverageIds());

        if (quoteCoverages.size() != quoteRequest.coverageIds().size()) {
            throw new InvalidQuoteRequestException("one or more coverages were not found");
        }

        boolean hasCoverageFromAnotherProduct = quoteCoverages.stream()
                .anyMatch(coverage -> !coverage.getProductId().equals(quoteRequest.productId()));

        if (hasCoverageFromAnotherProduct) {
            throw new InvalidQuoteRequestException("one or more coverages do not belong to the selected product");
        }

        List<CoveragePrice> quoteCoveragePrices = quoteCoverages.stream()
                .map(coverage -> new CoveragePrice(
                        coverage.getCode(),
                        coverage.getBasePrice()
                ))
                .toList();

        PremiumCalculationResult premiumCalculationResult = premiumCalculator.calculate(
                quoteCoveragePrices,
                quoteRequest.startDate(),
                quoteRequest.endDate()
        );
        QuoteEntity quoteEntity = new QuoteEntity(
                quoteRequest.productId(),
                quoteRequest.coverageIds(),
                quoteRequest.startDate(),
                quoteRequest.endDate(),
                premiumCalculationResult.netPremium(),
                premiumCalculationResult.taxAmount(),
                premiumCalculationResult.totalAmount(),
                Instant.now()
        );

        QuoteEntity savedQuote = quoteRepository.save(quoteEntity);
        return quoteMapper.toResponse(savedQuote);
    }
}
