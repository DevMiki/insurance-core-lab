package com.codercollie.insurance_lab_core.mapper;

import com.codercollie.insurance_lab_core.dto.claim.ClaimResponse;
import com.codercollie.insurance_lab_core.persistence.entity.ClaimEntity;
import org.springframework.stereotype.Component;

@Component
public class ClaimMapper {

    public ClaimResponse toResponse(ClaimEntity entity) {
        return new ClaimResponse(
                entity.getId(),
                entity.getClaimNumber(),
                entity.getPolicy().getId(),
                entity.getLossDate(),
                entity.getNoticeDate(),
                entity.getClaimedAmount(),
                entity.getStatus()
        );
    }
}
