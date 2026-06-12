package com.codercollie.insurance_lab_core.mapper;

import com.codercollie.insurance_lab_core.dto.quote.QuoteResponse;
import com.codercollie.insurance_lab_core.persistence.entity.QuoteEntity;
import org.springframework.stereotype.Component;

@Component
public class QuoteMapper {

    public QuoteResponse toResponse(QuoteEntity entity) {
        return new QuoteResponse(
                entity.getId(),
                entity.getProductId(),
                entity.getCoverageIds(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getNetPremium(),
                entity.getTaxAmount(),
                entity.getTotalAmount(),
                entity.getCreatedAt()
        );
    }
}
