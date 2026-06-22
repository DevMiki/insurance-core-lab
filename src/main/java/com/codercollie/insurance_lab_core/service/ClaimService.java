package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.domain.ClaimStatus;
import com.codercollie.insurance_lab_core.domain.PolicyStatus;
import com.codercollie.insurance_lab_core.dto.claim.ClaimResponse;
import com.codercollie.insurance_lab_core.dto.claim.CreateClaimRequest;
import com.codercollie.insurance_lab_core.exception.InvalidClaimRequestException;
import com.codercollie.insurance_lab_core.exception.ResourceNotFoundException;
import com.codercollie.insurance_lab_core.mapper.ClaimMapper;
import com.codercollie.insurance_lab_core.persistence.entity.ClaimEntity;
import com.codercollie.insurance_lab_core.persistence.entity.ClaimMovementEntity;
import com.codercollie.insurance_lab_core.persistence.entity.PolicyEntity;
import com.codercollie.insurance_lab_core.repository.ClaimMovementRepository;
import com.codercollie.insurance_lab_core.repository.ClaimRepository;
import com.codercollie.insurance_lab_core.repository.PolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final ClaimMovementRepository claimMovementRepository;
    private final ClaimMapper claimMapper;

    public ClaimService(
            ClaimRepository claimRepository,
            PolicyRepository policyRepository,
            ClaimMovementRepository claimMovementRepository,
            ClaimMapper claimMapper
    ) {
        this.claimRepository = claimRepository;
        this.policyRepository = policyRepository;
        this.claimMovementRepository = claimMovementRepository;
        this.claimMapper = claimMapper;
    }

    @Transactional(readOnly = true)
    public ClaimResponse getClaimById(Long claimId) {
        final ClaimEntity claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("claim not found"));

        return claimMapper.toResponse(claim);
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> getClaimsByPolicyId(Long policyId) {
        if (!policyRepository.existsById(policyId)) {
            throw new ResourceNotFoundException("policy not found");
        }

        return claimRepository.findByPolicyIdOrderByIdAsc(policyId)
                .stream()
                .map(claimMapper::toResponse)
                .toList();
    }

    public ClaimResponse openClaim(CreateClaimRequest claimRequest) {
        final PolicyEntity policy = policyRepository.findById(claimRequest.policyId())
                .orElseThrow(() -> new ResourceNotFoundException("policy not found"));

        if (policy.getStatus() != PolicyStatus.ACTIVE) {
            throw new InvalidClaimRequestException(
                    "claim can be opened only on an active policy"
            );
        }

        if (claimRequest.lossDate().isBefore(policy.getStartDate())
                || claimRequest.lossDate().isAfter(policy.getEndDate())) {
            throw new InvalidClaimRequestException(
                    "lossDate must be inside the policy period"
            );
        }

        final String claimNumber = "CLM-" + UUID.randomUUID();

        final ClaimEntity claim = claimMapper.toEntity(claimNumber, policy, claimRequest);
        final ClaimEntity savedClaim = claimRepository.save(claim);

        final ClaimMovementEntity openingMovement = new ClaimMovementEntity(
                savedClaim,
                ClaimStatus.OPENED,
                null,
                "Claim opened",
                Instant.now()
        );
        claimMovementRepository.save(openingMovement);
        return claimMapper.toResponse(savedClaim);
    }
}
