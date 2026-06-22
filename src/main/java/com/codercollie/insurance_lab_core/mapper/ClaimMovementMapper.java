package com.codercollie.insurance_lab_core.mapper;

import com.codercollie.insurance_lab_core.dto.claim_movement.ClaimMovementResponse;
import com.codercollie.insurance_lab_core.persistence.entity.ClaimMovementEntity;
import org.springframework.stereotype.Component;

@Component
public class ClaimMovementMapper {

    public ClaimMovementResponse toResponse(ClaimMovementEntity entity) {
        return new ClaimMovementResponse(
                entity.getId(),
                entity.getClaim().getId(),
                entity.getStatus(),
                entity.getAmount(),
                entity.getNote(),
                entity.getCreatedAt()
        );
    }
}
