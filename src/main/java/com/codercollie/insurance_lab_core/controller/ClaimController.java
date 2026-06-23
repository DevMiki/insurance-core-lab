package com.codercollie.insurance_lab_core.controller;

import com.codercollie.insurance_lab_core.dto.claim.ClaimResponse;
import com.codercollie.insurance_lab_core.dto.claim.CreateClaimRequest;
import com.codercollie.insurance_lab_core.dto.claim.ReserveClaimRequest;
import com.codercollie.insurance_lab_core.dto.claim.SettleClaimRequest;
import com.codercollie.insurance_lab_core.service.ClaimLifecycleService;
import com.codercollie.insurance_lab_core.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ClaimController {

    private final ClaimService claimService;
    private final ClaimLifecycleService claimLifecycleService;

    public ClaimController(ClaimService claimService, ClaimLifecycleService claimLifecycleService) {
        this.claimService = claimService;
        this.claimLifecycleService = claimLifecycleService;
    }

    @GetMapping("/claims/{id}")
    public ClaimResponse getClaimById(@PathVariable Long id) {
        return claimService.getClaimById(id);
    }

    @GetMapping("/policies/{policyId}/claims")
    public List<ClaimResponse> getClaimsByPolicyId(@PathVariable Long policyId) {
        return claimService.getClaimsByPolicyId(policyId);
    }

    @PostMapping("/claims")
    @ResponseStatus(HttpStatus.CREATED)
    public ClaimResponse openClaim(@Valid @RequestBody CreateClaimRequest claimRequest) {
        return claimService.openClaim(claimRequest);
    }

    @PostMapping("/claims/{claimId}/reserve")
    public ClaimResponse reserveClaim(
            @PathVariable Long claimId,
            @Valid @RequestBody ReserveClaimRequest reserveRequest
    ) {
        return claimLifecycleService.reserveClaim(claimId, reserveRequest);
    }

    @PostMapping("/claims/{claimId}/settle")
    public ClaimResponse settleClaim(
            @PathVariable Long claimId,
            @Valid @RequestBody SettleClaimRequest settleClaimRequest
    ) {
        return claimLifecycleService.settleClaim(claimId, settleClaimRequest);
    }
}
