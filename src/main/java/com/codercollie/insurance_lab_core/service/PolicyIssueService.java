package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.domain.PolicyStatus;
import com.codercollie.insurance_lab_core.dto.policy.PolicyResponse;
import com.codercollie.insurance_lab_core.exception.InvalidPolicyIssueRequestException;
import com.codercollie.insurance_lab_core.exception.ResourceNotFoundException;
import com.codercollie.insurance_lab_core.mapper.PolicyMapper;
import com.codercollie.insurance_lab_core.persistence.entity.PolicyEntity;
import com.codercollie.insurance_lab_core.persistence.entity.PremiumEntity;
import com.codercollie.insurance_lab_core.persistence.entity.QuoteEntity;
import com.codercollie.insurance_lab_core.repository.PolicyRepository;
import com.codercollie.insurance_lab_core.repository.PremiumRepository;
import com.codercollie.insurance_lab_core.repository.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashSet;

@Service
@Transactional
public class PolicyIssueService {

    private final QuoteRepository quoteRepository;
    private final PolicyRepository policyRepository;
    private final PremiumRepository premiumRepository;
    private final PolicyMapper policyMapper;

    public PolicyIssueService(
            QuoteRepository quoteRepository,
            PolicyRepository policyRepository,
            PremiumRepository premiumRepository,
            PolicyMapper policyMapper
    ) {
        this.quoteRepository = quoteRepository;
        this.policyRepository = policyRepository;
        this.premiumRepository = premiumRepository;
        this.policyMapper = policyMapper;
    }

    public PolicyResponse issueQuote(Long quoteId) {

        QuoteEntity quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("quote not found"));

        if (policyRepository.existsByQuoteId(quote.getId())) {
            throw new InvalidPolicyIssueRequestException("quote has already been issued");
        }

        PolicyEntity policy = new PolicyEntity(
                generatePolicyNumber(quote),
                quote.getId(),
                quote.getCustomerId(),
                quote.getProductId(),
                new LinkedHashSet<>(quote.getCoverages()),
                quote.getStartDate(),
                quote.getEndDate(),
                PolicyStatus.ISSUED
        );

        PolicyEntity savedPolicy = policyRepository.save(policy);
        PremiumEntity premium = new PremiumEntity(
                savedPolicy,
                quote.getTotalAmount(),
                quote.getStartDate()
        );
        premiumRepository.save(premium);
        return policyMapper.toResponse(savedPolicy);
    }

    private String generatePolicyNumber(QuoteEntity quote) {
        return "POL-" + LocalDate.now().getYear() + "-" + String.format("%06d", quote.getId());
    }

}
