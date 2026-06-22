package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.dto.claim.ClaimResponse;
import com.codercollie.insurance_lab_core.dto.claim.ReserveClaimRequest;
import com.codercollie.insurance_lab_core.mapper.ClaimMapper;
import com.codercollie.insurance_lab_core.repository.ClaimMovementRepository;
import com.codercollie.insurance_lab_core.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public ClaimResponse reserveClaim(Long claimId, ReserveClaimRequest request) {
        throw new UnsupportedOperationException("not implemented yet");
    }
}
