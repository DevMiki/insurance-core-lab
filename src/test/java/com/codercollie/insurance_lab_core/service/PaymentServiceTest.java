package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.domain.PaymentStatus;
import com.codercollie.insurance_lab_core.domain.PolicyStatus;
import com.codercollie.insurance_lab_core.dto.payment.CreatePaymentRequest;
import com.codercollie.insurance_lab_core.dto.payment.PaymentResponse;
import com.codercollie.insurance_lab_core.exception.InvalidPaymentRequestException;
import com.codercollie.insurance_lab_core.exception.ResourceNotFoundException;
import com.codercollie.insurance_lab_core.mapper.PaymentMapper;
import com.codercollie.insurance_lab_core.persistence.entity.PaymentEntity;
import com.codercollie.insurance_lab_core.persistence.entity.PolicyEntity;
import com.codercollie.insurance_lab_core.persistence.entity.PremiumEntity;
import com.codercollie.insurance_lab_core.repository.PaymentRepository;
import com.codercollie.insurance_lab_core.repository.PolicyRepository;
import com.codercollie.insurance_lab_core.repository.PremiumRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
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
                new BigDecimal("50.00"),
                LocalDate.of(2026, 6, 16),
                PaymentStatus.PENDING
        );

        when(paymentRepository.existsByExternalReference(request.externalReference()))
                .thenReturn(true);

        InvalidPaymentRequestException exception = assertThrows(
                InvalidPaymentRequestException.class,
                () -> paymentService.createPayment(10L, request));

        assertEquals("payment externalReference already exists", exception.getMessage());
        verifyNoInteractions(policyRepository, premiumRepository);
    }

    @Test
    void throwsWhenPolicyIdDoesNotExist() {
        CreatePaymentRequest request = new CreatePaymentRequest(
                "BANK-TXN-123",
                new BigDecimal("100.00"),
                LocalDate.of(2026, 6, 16),
                PaymentStatus.PAID
        );

        when(paymentRepository.existsByExternalReference(request.externalReference()))
                .thenReturn(false);
        when(policyRepository.findById(10L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.createPayment(10L, request)
        );

        assertEquals("policy not found", exception.getMessage());
    }

    @Test
    void createsPaymentForExistingPolicy() {
        CreatePaymentRequest request = new CreatePaymentRequest(
                "BANK-TXN-123",
                new BigDecimal("50.00"),
                LocalDate.of(2026, 6, 16),
                PaymentStatus.PENDING
        );

        PolicyEntity policy = new PolicyEntity(
                "POL-2026-000001",
                null,
                1L,
                1L,
                Set.of(),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 1),
                PolicyStatus.ISSUED
        );
        ReflectionTestUtils.setField(policy, "id", 10L);

        when(paymentRepository.existsByExternalReference(request.externalReference()))
                .thenReturn(false);
        when(policyRepository.findById(10L))
                .thenReturn(Optional.of(policy));

        when(paymentRepository.save(any(PaymentEntity.class)))
                .thenAnswer(invocation -> {
                    PaymentEntity payment = invocation.getArgument(0);
                    ReflectionTestUtils.setField(payment, "id", 99L);
                    return payment;
                });

        PaymentResponse response = paymentService.createPayment(10L, request);

        assertEquals(99L, response.id());
        assertEquals("BANK-TXN-123", response.externalReference());
        assertEquals(10L, response.policyId());
        assertEquals(new BigDecimal("50.00"), response.amount());
        assertEquals(LocalDate.of(2026, 6, 16), response.paymentDate());
        assertEquals(PaymentStatus.PENDING, response.status());

        verify(paymentRepository).save(ArgumentMatchers.any(PaymentEntity.class));
    }

    @Test
    void createsFailedPaymentWithoutActivatingPolicy() {
        PolicyEntity policy = new PolicyEntity(
                "POL-2026-000001",
                null,
                1L,
                1L,
                Set.of(),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 1),
                PolicyStatus.ISSUED
        );
        ReflectionTestUtils.setField(policy, "id", 10L);

        CreatePaymentRequest request = new CreatePaymentRequest(
                "BANK-TXN-FAILED-123",
                new BigDecimal("100.00"),
                LocalDate.of(2026, 6, 16),
                PaymentStatus.FAILED
        );

        when(paymentRepository.existsByExternalReference(request.externalReference()))
                .thenReturn(false);
        when(policyRepository.findById(10L))
                .thenReturn(Optional.of(policy));
        when(paymentRepository.save(any(PaymentEntity.class)))
                .thenAnswer(invocation -> {
                    PaymentEntity payment = invocation.getArgument(0);
                    ReflectionTestUtils.setField(payment, "id", 100L);
                    return payment;
                });

        PaymentResponse response = paymentService.createPayment(10L, request);

        assertEquals(100L, response.id());
        assertEquals("BANK-TXN-FAILED-123", response.externalReference());
        assertEquals(10L, response.policyId());
        assertEquals(new BigDecimal("100.00"), response.amount());
        assertEquals(PaymentStatus.FAILED, response.status());
        assertEquals(PolicyStatus.ISSUED, policy.getStatus());

        verify(paymentRepository).save(ArgumentMatchers.any(PaymentEntity.class));
    }

    @Test
    void activatesPolicyWhenPaidPaymentCoversPremium() {
        PolicyEntity policy = new PolicyEntity(
                "POL-2026-000001",
                null,
                1L,
                1L,
                Set.of(),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 1),
                PolicyStatus.ISSUED
        );
        ReflectionTestUtils.setField(policy, "id", 10L);

        CreatePaymentRequest request = new CreatePaymentRequest(
                "BANK-TXN-PAID-123",
                new BigDecimal("100.00"),
                LocalDate.of(2026, 6, 16),
                PaymentStatus.PAID
        );

        PremiumEntity premium = new PremiumEntity(
                policy,
                new BigDecimal("100.00"),
                LocalDate.of(2026, 1, 1)
        );

        when(paymentRepository.existsByExternalReference(request.externalReference()))
                .thenReturn(false);
        when(policyRepository.findById(10L))
                .thenReturn(Optional.of(policy));
        when(premiumRepository.findByPolicyId(10L))
                .thenReturn(Optional.of(premium));
        when(paymentRepository.save(any(PaymentEntity.class)))
                .thenAnswer(invocation -> {
                    PaymentEntity payment = invocation.getArgument(0);
                    ReflectionTestUtils.setField(payment, "id", 101L);
                    return payment;
                });

        paymentService.createPayment(10L, request);

        assertEquals(PolicyStatus.ACTIVE, policy.getStatus());
    }

    @Test
    void doesNotActivatePolicyWhenPaidPaymentIsLessThanPremium() {
        PolicyEntity policy = new PolicyEntity(
                "POL-2026-000001",
                null,
                1L,
                1L,
                Set.of(),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 1),
                PolicyStatus.ISSUED
        );
        ReflectionTestUtils.setField(policy, "id", 10L);

        CreatePaymentRequest request = new CreatePaymentRequest(
                "BANK-TXN-PARTIAL-123",
                new BigDecimal("50.00"),
                LocalDate.of(2026, 6, 16),
                PaymentStatus.PAID
        );

        PremiumEntity premium = new PremiumEntity(
                policy,
                new BigDecimal("100.00"),
                LocalDate.of(2026, 1, 1)
        );

        when(paymentRepository.existsByExternalReference(request.externalReference()))
                .thenReturn(false);
        when(policyRepository.findById(10L))
                .thenReturn(Optional.of(policy));
        when(premiumRepository.findByPolicyId(10L))
                .thenReturn(Optional.of(premium));
        when(paymentRepository.save(any(PaymentEntity.class)))
                .thenAnswer(invocation -> {
                    PaymentEntity payment = invocation.getArgument(0);
                    ReflectionTestUtils.setField(payment, "id", 102L);
                    return payment;
                });

        paymentService.createPayment(10L, request);

        assertEquals(PolicyStatus.ISSUED, policy.getStatus());
    }

    @Test
    void activatesPolicyWhenTotalPaidPaymentsCoverPremium() {
        PolicyEntity policy = new PolicyEntity(
                "POL-2026-000001",
                null,
                1L,
                1L,
                Set.of(),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 1),
                PolicyStatus.ISSUED
        );
        ReflectionTestUtils.setField(policy, "id", 10L);

        CreatePaymentRequest request = new CreatePaymentRequest(
                "BANK-TXN-FINAL-123",
                new BigDecimal("60.00"),
                LocalDate.of(2026, 6, 16),
                PaymentStatus.PAID
        );

        PremiumEntity premium = new PremiumEntity(
                policy,
                new BigDecimal("100.00"),
                LocalDate.of(2026, 1, 1)
        );

        PaymentEntity previousPayment = new PaymentEntity(
                "BANK-TXN-FIRST-123",
                policy,
                new BigDecimal("40.00"),
                LocalDate.of(2026, 6, 15),
                PaymentStatus.PAID
        );

        when(paymentRepository.existsByExternalReference(request.externalReference()))
                .thenReturn(false);
        when(policyRepository.findById(10L))
                .thenReturn(Optional.of(policy));
        when(premiumRepository.findByPolicyId(10L))
                .thenReturn(Optional.of(premium));

        when(paymentRepository.findByPolicyIdOrderByPaymentDateAscIdAsc(10L))
                .thenReturn(List.of(previousPayment));
        when(paymentRepository.save(any(PaymentEntity.class)))
                .thenAnswer(invocation -> {
                    PaymentEntity payment = invocation.getArgument(0);
                    ReflectionTestUtils.setField(payment, "id", 103L);
                    return payment;
                });

        paymentService.createPayment(10L, request);

        assertEquals(PolicyStatus.ACTIVE, policy.getStatus());
    }
}
