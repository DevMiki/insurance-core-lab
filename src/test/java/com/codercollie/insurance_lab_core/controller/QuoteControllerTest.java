package com.codercollie.insurance_lab_core.controller;

import com.codercollie.insurance_lab_core.dto.quote.CreateQuoteRequest;
import com.codercollie.insurance_lab_core.dto.quote.QuoteResponse;
import com.codercollie.insurance_lab_core.service.QuoteService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuoteControllerTest {

    private final QuoteService quoteService = mock(QuoteService.class);
    private final QuoteController quoteController = new QuoteController(quoteService);

    @Test
    void createsQuote() {
        CreateQuoteRequest request = new CreateQuoteRequest(
                1L,
                List.of(10L, 11L),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 11)
        );

        QuoteResponse expectedResponse = quoteResponse();

        when(quoteService.createQuote(request)).thenReturn(expectedResponse);

        QuoteResponse response = quoteController.createQuote(request);

        assertEquals(expectedResponse, response);
    }

    @Test
    void getsQuoteById() {
        QuoteResponse expectedResponse = quoteResponse();

        when(quoteService.getQuoteById(99L)).thenReturn(expectedResponse);

        QuoteResponse response = quoteController.getQuoteById(99L);

        assertEquals(expectedResponse, response);
    }

    private QuoteResponse quoteResponse() {
        return new QuoteResponse(
                99L,
                1L,
                List.of(10L, 11L),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 11),
                new BigDecimal("1500.00"),
                new BigDecimal("330.00"),
                new BigDecimal("1830.00"),
                Instant.parse("2026-01-01T10:00:00Z")
        );
    }
}
