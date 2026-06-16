package com.codercollie.insurance_lab_core.mapper;

import com.codercollie.insurance_lab_core.dto.quote.QuoteResponse;
import com.codercollie.insurance_lab_core.persistence.entity.QuoteEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuoteMapper {

    private final CoverageIdMapper coverageIdMapper;

    public QuoteMapper(CoverageIdMapper coverageIdMapper) {
        this.coverageIdMapper = coverageIdMapper;
    }

    public QuoteResponse toResponse(QuoteEntity entity) {
        List<Long> coverageIds = coverageIdMapper.toSortedIds(entity.getCoverages());

        return new QuoteResponse(
                entity.getId(),
                entity.getProductId(),
                entity.getCustomerId(),
                coverageIds,
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getNetPremium(),
                entity.getTaxAmount(),
                entity.getTotalAmount(),
                entity.getCreatedAt()
        );
    }
}
