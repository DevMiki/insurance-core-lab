package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.mapper.ClaimMapper;
import com.codercollie.insurance_lab_core.repository.ClaimRepository;
import com.codercollie.insurance_lab_core.repository.PolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
