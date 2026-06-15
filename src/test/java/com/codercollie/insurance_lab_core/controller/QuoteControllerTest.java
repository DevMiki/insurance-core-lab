package com.codercollie.insurance_lab_core.controller;

import com.codercollie.insurance_lab_core.dto.quote.CreateQuoteRequest;
import com.codercollie.insurance_lab_core.dto.quote.QuoteResponse;
import com.codercollie.insurance_lab_core.exception.InvalidQuoteRequestException;
import com.codercollie.insurance_lab_core.exception.ResourceNotFoundException;
import com.codercollie.insurance_lab_core.service.QuoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QuoteController.class)
class QuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QuoteService quoteService;

    @Test
    void createsQuote() throws Exception {
        CreateQuoteRequest request = new CreateQuoteRequest(
                1L,
                2L,
                List.of(10L, 11L),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 11)
        );

        when(quoteService.createQuote(any(CreateQuoteRequest.class))).thenReturn(quoteResponse());

        mockMvc.perform(post("/api/v1/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.customerId", is(2)))
                .andExpect(jsonPath("$.coverageIds[0]", is(10)))
                .andExpect(jsonPath("$.coverageIds[1]", is(11)))
                .andExpect(jsonPath("$.totalAmount", is(1830.00)));

        verify(quoteService).createQuote(request);
    }

    @Test
    void getsQuoteById() throws Exception {
        when(quoteService.getQuoteById(99L)).thenReturn(quoteResponse());

        mockMvc.perform(get("/api/v1/quotes/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.productId", is(1)))
                .andExpect(jsonPath("$.customerId", is(2)))
                .andExpect(jsonPath("$.totalAmount", is(1830.00)));

        verify(quoteService).getQuoteById(99L);
    }

    @Test
    void returnsNotFoundWhenQuoteDoesNotExist() throws Exception {
        when(quoteService.getQuoteById(999L))
                .thenThrow(new ResourceNotFoundException("quote not found"));

        mockMvc.perform(get("/api/v1/quotes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("quote not found")));

        verify(quoteService).getQuoteById(999L);
    }

    @Test
    void returnsNotFoundWhenCreatingQuoteForMissingProduct() throws Exception {
        CreateQuoteRequest request = new CreateQuoteRequest(
                999L,
                2L,
                List.of(10L),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 11)
        );

        when(quoteService.createQuote(any(CreateQuoteRequest.class)))
                .thenThrow(new ResourceNotFoundException("product not found"));

        mockMvc.perform(post("/api/v1/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("product not found")));
    }

    @Test
    void returnsBadRequestForInvalidQuoteRequest() throws Exception {
        CreateQuoteRequest request = new CreateQuoteRequest(
                1L,
                2L,
                List.of(10L, 999L),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 11)
        );

        when(quoteService.createQuote(any(CreateQuoteRequest.class)))
                .thenThrow(new InvalidQuoteRequestException("one or more coverages were not found"));

        mockMvc.perform(post("/api/v1/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("one or more coverages were not found")));
    }

    @Test
    void rejectsInvalidQuoteDates() throws Exception {
        CreateQuoteRequest request = new CreateQuoteRequest(
                1L,
                2L,
                List.of(10L),
                LocalDate.of(2026, 1, 11),
                LocalDate.of(2026, 1, 1)
        );

        mockMvc.perform(post("/api/v1/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("endDate must be after startDate")));

        verify(quoteService, never()).createQuote(any(CreateQuoteRequest.class));
    }

    @Test
    void rejectsMissingCoverages() throws Exception {
        CreateQuoteRequest request = new CreateQuoteRequest(
                1L,
                2L,
                List.of(),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 11)
        );

        mockMvc.perform(post("/api/v1/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("coverageIds are required")));

        verify(quoteService, never()).createQuote(any(CreateQuoteRequest.class));
    }

    @Test
    void rejectsMissingProductId() throws Exception {
        CreateQuoteRequest request = new CreateQuoteRequest(
                null,
                2L,
                List.of(10L),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 11)
        );

        mockMvc.perform(post("/api/v1/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("productId is required")));

        verify(quoteService, never()).createQuote(any(CreateQuoteRequest.class));
    }

    @Test
    void rejectsMissingCustomerId() throws Exception {
        CreateQuoteRequest request = new CreateQuoteRequest(
                1L,
                null,
                List.of(10L),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 11)
        );

        mockMvc.perform(post("/api/v1/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("customerId is required")));

        verify(quoteService, never()).createQuote(any(CreateQuoteRequest.class));
    }

    @Test
    void rejectsMissingStartDate() throws Exception {
        CreateQuoteRequest request = new CreateQuoteRequest(
                1L,
                2L,
                List.of(10L),
                null,
                LocalDate.of(2026, 1, 11)
        );

        mockMvc.perform(post("/api/v1/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("startDate is required")));

        verify(quoteService, never()).createQuote(any(CreateQuoteRequest.class));
    }

    @Test
    void rejectsMissingEndDate() throws Exception {
        CreateQuoteRequest request = new CreateQuoteRequest(
                1L,
                2L,
                List.of(10L),
                LocalDate.of(2026, 1, 1),
                null
        );

        mockMvc.perform(post("/api/v1/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("endDate is required")));

        verify(quoteService, never()).createQuote(any(CreateQuoteRequest.class));
    }

    private QuoteResponse quoteResponse() {
        return new QuoteResponse(
                99L,
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
    }
}
