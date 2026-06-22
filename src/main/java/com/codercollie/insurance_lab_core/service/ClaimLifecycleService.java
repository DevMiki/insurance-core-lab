package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.dto.claim.ClaimResponse;
import com.codercollie.insurance_lab_core.dto.claim.ReserveClaimRequest;
import com.codercollie.insurance_lab_core.exception.ResourceNotFoundException;
import com.codercollie.insurance_lab_core.mapper.ClaimMapper;
import com.codercollie.insurance_lab_core.persistence.entity.ClaimEntity;
import com.codercollie.insurance_lab_core.persistence.entity.ClaimMovementEntity;
import com.codercollie.insurance_lab_core.repository.ClaimMovementRepository;
import com.codercollie.insurance_lab_core.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class ClaimLifecycleService {

    private final ClaimRepository claimRepository;
    private final ClaimMovementRepository claimMovementRepository;
    private final ClaimMapper claimMapper;

    public ClaimLifecycleService(
            ClaimRepository claimRepository,
            ClaimMovementRepository claimMovementRepository,
            ClaimMapper claimMapper
    ) {
        this.claimRepository = claimRepository;
        this.claimMovementRepository = claimMovementRepository;
        this.claimMapper = claimMapper;
    }

    public ClaimResponse reserveClaim(
            Long claimId,
            ReserveClaimRequest request
    ) {
        final ClaimEntity claim = claimRepository.findById(claimId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("claim not found")
                );

        claim.reserve(request.amount());

        final ClaimMovementEntity reserveMovement = new ClaimMovementEntity(
                claim,
                claim.getStatus(),
                claim.getReservedAmount(),
                "Reserve set",
                Instant.now()
        );

        claimMovementRepository.save(reserveMovement);
        return claimMapper.toResponse(claim);
    }
}
