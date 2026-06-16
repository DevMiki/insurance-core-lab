package com.codercollie.insurance_lab_core.mapper;

import com.codercollie.insurance_lab_core.dto.policy.PolicyResponse;
import com.codercollie.insurance_lab_core.persistence.entity.PolicyEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PolicyMapper {

    private final CoverageIdMapper coverageIdMapper;

    public PolicyMapper(CoverageIdMapper coverageIdMapper) {
        this.coverageIdMapper = coverageIdMapper;
    }

    public PolicyResponse toResponse(PolicyEntity entity) {
        List<Long> coverageIds = coverageIdMapper.toSortedIds(entity.getCoverages());

        return new PolicyResponse(
                entity.getId(),
                entity.getPolicyNumber(),
                entity.getQuoteId(),
                entity.getCustomerId(),
                entity.getProductId(),
                coverageIds,
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getStatus()
        );
    }

}
