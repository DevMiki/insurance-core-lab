package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.domain.PaymentStatus;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;
    private final PremiumRepository premiumRepository;

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

    private final PaymentMapper paymentMapper;

    public PaymentResponse createPayment(Long policyId, CreatePaymentRequest paymentRequest) {

        if (paymentRepository.existsByExternalReference(paymentRequest.externalReference())) {
            throw new InvalidPaymentRequestException("payment externalReference already exists");
        }

        PolicyEntity policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("policy not found"));

        PaymentEntity paymentEntity = paymentMapper.toEntity(policy, paymentRequest);
        PaymentEntity savedEntity = paymentRepository.save(paymentEntity);

        if (paymentRequest.status() == PaymentStatus.PAID) {
            PremiumEntity premium = premiumRepository.findByPolicyId(policyId)
                    .orElseThrow(() -> new ResourceNotFoundException("premium not found"));

            if (paymentRequest.amount().compareTo(premium.getAmount()) >= 0) {
                policy.activate();
            }
        }

        return paymentMapper.toResponse(savedEntity);
    }

}
