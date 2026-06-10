package com.codercollie.insurance_lab_core.mapper;

import com.codercollie.insurance_lab_core.dto.coverage.CoverageResponse;
import com.codercollie.insurance_lab_core.dto.coverage.CreateCoverageRequest;
import com.codercollie.insurance_lab_core.persistence.entity.CoverageEntity;
import org.springframework.stereotype.Component;

@Component
public class CoverageMapper {

    public CoverageEntity toEntity(CreateCoverageRequest request) {
        return new CoverageEntity(
                request.code(),
                request.name(),
                request.description(),
                request.basePrice()
        );
    }

    public CoverageResponse toResponse(CoverageEntity entity) {
        return new CoverageResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getBasePrice(),
                entity.getProductId()
        );
    }
}
