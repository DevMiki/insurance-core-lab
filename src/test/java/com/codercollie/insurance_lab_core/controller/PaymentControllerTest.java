package com.codercollie.insurance_lab_core.controller;

import com.codercollie.insurance_lab_core.domain.PaymentStatus;
import com.codercollie.insurance_lab_core.dto.payment.CreatePaymentRequest;
import com.codercollie.insurance_lab_core.dto.payment.PaymentResponse;
import com.codercollie.insurance_lab_core.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void returnsPaymentsForPolicy() throws Exception {
        when(paymentService.getPaymentsForPolicy(10L))
                .thenReturn(List.of(
                        new PaymentResponse(
                                201L,
                                "BANK-TXN-001",
                                10L,
                                new BigDecimal("40.00"),
                                LocalDate.of(2026, 6, 15),
                                PaymentStatus.PAID
                        )
                ));

        mockMvc.perform(get("/api/v1/policies/10/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(201)))
                .andExpect(jsonPath("$[0].externalReference", is("BANK-TXN-001")))
                .andExpect(jsonPath("$[0].policyId", is(10)))
                .andExpect(jsonPath("$[0].amount", is(40.00)))
                .andExpect(jsonPath("$[0].paymentDate", is("2026-06-15")))
                .andExpect(jsonPath("$[0].status", is("PAID")));

        verify(paymentService).getPaymentsForPolicy(10L);
    }

    @Test
    void returnsCreatedPaymentWhenRequestIsValid() throws Exception {
        when(paymentService.createPayment(any(Long.class), any(CreatePaymentRequest.class)))
                .thenReturn(new PaymentResponse(
                        201L,
                        "BANK-TXN-001",
                        10L,
                        new BigDecimal("40.00"),
                        LocalDate.of(2026, 6, 15),
                        PaymentStatus.PAID
                ));

        CreatePaymentRequest paymentRequest = new CreatePaymentRequest(
                "BANK-TXN-001",
                new BigDecimal("40.00"),
                LocalDate.of(2026, 6, 15),
                PaymentStatus.PAID
        );

        mockMvc.perform(post("/api/v1/policies/{policyId}/payments", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(201)))
                .andExpect(jsonPath("$.externalReference", is("BANK-TXN-001")))
                .andExpect(jsonPath("$.policyId", is(10)))
                .andExpect(jsonPath("$.amount", is(40.00)))
                .andExpect(jsonPath("$.paymentDate", is("2026-06-15")))
                .andExpect(jsonPath("$.status", is("PAID")));

        verify(paymentService).createPayment(10L, paymentRequest);
    }

}
