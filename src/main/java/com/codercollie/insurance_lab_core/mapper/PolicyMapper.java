package com.codercollie.insurance_lab_core.mapper;

import com.codercollie.insurance_lab_core.dto.policy.PolicyResponse;
import com.codercollie.insurance_lab_core.persistence.entity.CoverageEntity;
import com.codercollie.insurance_lab_core.persistence.entity.PolicyEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PolicyMapper {

    public PolicyResponse toResponse(PolicyEntity entity) {
        List<Long> coverageIds = entity.getCoverages()
                .stream()
                .map(CoverageEntity::getId)
                .toList();

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
