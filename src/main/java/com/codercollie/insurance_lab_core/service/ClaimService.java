package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.domain.PolicyStatus;
import com.codercollie.insurance_lab_core.dto.claim.ClaimResponse;
import com.codercollie.insurance_lab_core.dto.claim.CreateClaimRequest;
import com.codercollie.insurance_lab_core.exception.InvalidClaimRequestException;
import com.codercollie.insurance_lab_core.exception.ResourceNotFoundException;
import com.codercollie.insurance_lab_core.mapper.ClaimMapper;
import com.codercollie.insurance_lab_core.persistence.entity.ClaimEntity;
import com.codercollie.insurance_lab_core.persistence.entity.PolicyEntity;
import com.codercollie.insurance_lab_core.repository.ClaimRepository;
import com.codercollie.insurance_lab_core.repository.PolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final ClaimMapper claimMapper;

    public ClaimService(
            ClaimRepository claimRepository,
            PolicyRepository policyRepository,
            ClaimMapper claimMapper
    ) {
        this.claimRepository = claimRepository;
        this.policyRepository = policyRepository;
        this.claimMapper = claimMapper;
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
        return claimMapper.toResponse(savedClaim);
    }
}
