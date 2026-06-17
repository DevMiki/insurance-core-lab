package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.dto.payment.CreatePaymentRequest;
import com.codercollie.insurance_lab_core.dto.payment.PaymentResponse;
import com.codercollie.insurance_lab_core.exception.InvalidPaymentRequestException;
import com.codercollie.insurance_lab_core.mapper.PaymentMapper;
import com.codercollie.insurance_lab_core.repository.PaymentRepository;
import com.codercollie.insurance_lab_core.repository.PolicyRepository;
import com.codercollie.insurance_lab_core.repository.PremiumRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;
    private final PremiumRepository premiumRepository;
    private final PaymentMapper paymentMapper;

    public PaymentService(
            PaymentRepository paymentRepository,
            PolicyRepository policyRepository,
            PremiumRepository premiumRepository,
            PaymentMapper paymentMapper
    ) {
        this.paymentRepository = paymentRepository;
        this.policyRepository = policyRepository;
        this.premiumRepository = premiumRepository;
        this.paymentMapper = paymentMapper;
    }

    public PaymentResponse createPayment(Long policyId, CreatePaymentRequest paymentRequest) {

        if (paymentRepository.existsByExternalReference(paymentRequest.externalReference())) {
            throw new InvalidPaymentRequestException("payment externalReference already exists");
        }

        throw new UnsupportedOperationException("payment creation is not implemented yet");
    }

}
