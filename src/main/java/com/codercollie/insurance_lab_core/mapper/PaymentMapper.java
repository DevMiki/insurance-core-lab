package com.codercollie.insurance_lab_core.mapper;

import com.codercollie.insurance_lab_core.dto.payment.PaymentResponse;
import com.codercollie.insurance_lab_core.persistence.entity.PaymentEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(PaymentEntity entity) {
        return new PaymentResponse(
                entity.getId(),
                entity.getExternalReference(),
                entity.getPolicy().getId(),
                entity.getAmount(),
                entity.getPaymentDate(),
                entity.getStatus()
        );
    }
}
