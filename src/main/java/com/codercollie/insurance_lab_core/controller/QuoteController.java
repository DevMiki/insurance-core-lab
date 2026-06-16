package com.codercollie.insurance_lab_core.controller;

import com.codercollie.insurance_lab_core.dto.policy.PolicyResponse;
import com.codercollie.insurance_lab_core.dto.quote.CreateQuoteRequest;
import com.codercollie.insurance_lab_core.dto.quote.QuoteResponse;
import com.codercollie.insurance_lab_core.service.PolicyIssueService;
import com.codercollie.insurance_lab_core.service.QuoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quotes")
public class QuoteController {

    private final QuoteService quoteService;
    private final PolicyIssueService policyIssueService;

    public QuoteController(QuoteService quoteService, PolicyIssueService policyIssueService) {
        this.quoteService = quoteService;
        this.policyIssueService = policyIssueService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuoteResponse createQuote(@Valid @RequestBody CreateQuoteRequest createQuoteRequest) {
        return quoteService.createQuote(createQuoteRequest);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public QuoteResponse getQuoteById(@PathVariable Long id) {
        return quoteService.getQuoteById(id);
    }

    @PostMapping("/{quoteId}/issue")
    @ResponseStatus(HttpStatus.CREATED)
    public PolicyResponse issueQuote(@PathVariable Long quoteId) {
        return policyIssueService.issueQuote(quoteId);
    }
}
