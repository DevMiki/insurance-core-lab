package com.codercollie.insurance_lab_core.repository;

import com.codercollie.insurance_lab_core.domain.PaymentStatus;
import com.codercollie.insurance_lab_core.persistence.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    boolean existsByExternalReference(String externalReference);

    List<PaymentEntity> findByPolicyIdOrderByPaymentDateAscIdAsc(Long policyId);

    List<PaymentEntity> findByPolicyIdAndStatusOrderByPaymentDateAscIdAsc(Long policyId, PaymentStatus status);
}
