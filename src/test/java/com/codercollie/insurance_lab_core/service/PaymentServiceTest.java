package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.domain.PaymentStatus;
import com.codercollie.insurance_lab_core.dto.payment.CreatePaymentRequest;
import com.codercollie.insurance_lab_core.exception.InvalidPaymentRequestException;
import com.codercollie.insurance_lab_core.mapper.PaymentMapper;
import com.codercollie.insurance_lab_core.repository.PaymentRepository;
import com.codercollie.insurance_lab_core.repository.PolicyRepository;
import com.codercollie.insurance_lab_core.repository.PremiumRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PremiumRepository premiumRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(
                paymentRepository,
                policyRepository,
                premiumRepository,
                new PaymentMapper()
        );
    }

    @Test
    void throwsWhenDuplicateExternalReference() {
        CreatePaymentRequest request = new CreatePaymentRequest(
                "BANK-TXN-123",
                new BigDecimal("100.00"),
                LocalDate.of(2026, 6, 16),
                PaymentStatus.PAID
        );

        when(paymentRepository.existsByExternalReference(request.externalReference()))
                .thenReturn(true);

        InvalidPaymentRequestException exception = assertThrows(
                InvalidPaymentRequestException.class,
                () -> paymentService.createPayment(10L, request));

        assertEquals("payment externalReference already exists", exception.getMessage());
        verifyNoInteractions(policyRepository, premiumRepository);
    }
}
