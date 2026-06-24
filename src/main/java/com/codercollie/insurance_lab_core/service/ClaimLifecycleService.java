package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.domain.ClaimStatus;
import com.codercollie.insurance_lab_core.dto.claim.ClaimResponse;
import com.codercollie.insurance_lab_core.dto.claim.ReserveClaimRequest;
import com.codercollie.insurance_lab_core.dto.claim.SettleClaimRequest;
import com.codercollie.insurance_lab_core.dto.claim_movement.ClaimMovementResponse;
import com.codercollie.insurance_lab_core.exception.InvalidClaimRequestException;
import com.codercollie.insurance_lab_core.exception.ResourceNotFoundException;
import com.codercollie.insurance_lab_core.mapper.ClaimMapper;
import com.codercollie.insurance_lab_core.mapper.ClaimMovementMapper;
import com.codercollie.insurance_lab_core.persistence.entity.ClaimEntity;
import com.codercollie.insurance_lab_core.persistence.entity.ClaimMovementEntity;
import com.codercollie.insurance_lab_core.repository.ClaimMovementRepository;
import com.codercollie.insurance_lab_core.repository.ClaimRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class ClaimLifecycleService {

    private static final BigDecimal FAKE_MAX_PAYOUT = new BigDecimal("50000.00");

    private final ClaimRepository claimRepository;
    private final ClaimMovementRepository claimMovementRepository;
    private final ClaimMapper claimMapper;
    private final ClaimMovementMapper claimMovementMapper;

    public ClaimLifecycleService(
            ClaimRepository claimRepository,
            ClaimMovementRepository claimMovementRepository,
            ClaimMapper claimMapper,
            ClaimMovementMapper claimMovementMapper
    ) {
        this.claimRepository = claimRepository;
        this.claimMovementRepository = claimMovementRepository;
        this.claimMapper = claimMapper;
        this.claimMovementMapper = claimMovementMapper;
    }

    public ClaimResponse reserveClaim(Long claimId, ReserveClaimRequest request) {

        final ClaimEntity claim = claimRepository.findById(claimId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("claim not found"));

        if (claim.getStatus() != ClaimStatus.OPENED) {
            throw new InvalidClaimRequestException(
                    "only an opened claim can be reserved");
        }

        if (request.amount().compareTo(FAKE_MAX_PAYOUT) > 0) {
            throw new InvalidClaimRequestException(
                    "reserve amount must not exceed "
                            + FAKE_MAX_PAYOUT.toPlainString());
        }

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

    public ClaimResponse settleClaim(Long claimId, SettleClaimRequest settleClaimRequest) {
        ClaimEntity claim = claimRepository.findById(claimId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("claim not found"));

        if (claim.getStatus() != ClaimStatus.RESERVED) {
            throw new InvalidClaimRequestException(
                    "only a reserved claim can be settled");
        }

        if (settleClaimRequest.amount().compareTo(claim.getReservedAmount()) > 0) {
            throw new InvalidClaimRequestException(
                    "settlement amount must not exceed reserved amount");
        }

        claim.settle(settleClaimRequest.amount());

        ClaimMovementEntity settlementMovement = new ClaimMovementEntity(
                claim,
                claim.getStatus(),
                claim.getSettledAmount(),
                "Claim settled",
                Instant.now()
        );

        claimMovementRepository.save(settlementMovement);
        return claimMapper.toResponse(claim);
    }

    public ClaimResponse rejectClaim(Long claimId) {
        ClaimEntity claim = claimRepository.findById(claimId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("claim not found"));

        if (claim.getStatus() != ClaimStatus.OPENED) {
            throw new InvalidClaimRequestException(
                    "only an opened claim can be rejected");
        }

        claim.reject();

        final ClaimMovementEntity rejectionMovement = new ClaimMovementEntity(
                claim,
                claim.getStatus(),
                BigDecimal.ZERO,
                "Claim rejected",
                Instant.now()
        );

        claimMovementRepository.save(rejectionMovement);
        return claimMapper.toResponse(claim);
    }

    public List<ClaimMovementResponse> getClaimMovements(Long claimId) {
        claimRepository.findById(claimId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("claim not found"));

        return claimMovementRepository.findByClaimIdOrderByIdAsc(claimId)
                .stream()
                .map(claimMovementMapper::toResponse)
                .toList();
    }
}
