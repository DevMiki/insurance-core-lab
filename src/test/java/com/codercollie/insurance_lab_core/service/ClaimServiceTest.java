package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.domain.ClaimStatus;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private PolicyRepository policyRepository;

    private ClaimService claimService;

    @BeforeEach
    void setUp() {
        claimService = new ClaimService(
                claimRepository,
                policyRepository,
                new ClaimMapper()
        );
    }

    @Test
    void opensClaimForActivePolicy() {
        PolicyEntity policy = new PolicyEntity(
                "POL-2026-000001",
                null,
                1L,
                1L,
                Set.of(),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                PolicyStatus.ACTIVE
        );
        ReflectionTestUtils.setField(policy, "id", 10L);

        CreateClaimRequest request = new CreateClaimRequest(
                10L,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16),
                new BigDecimal("1500.00")
        );

        when(policyRepository.findById(10L))
                .thenReturn(Optional.of(policy));

        when(claimRepository.save(any(ClaimEntity.class)))
                .thenAnswer(invocation -> {
                    ClaimEntity claim = invocation.getArgument(0);
                    ReflectionTestUtils.setField(claim, "id", 99L);
                    return claim;
                });

        ClaimResponse response = claimService.openClaim(request);
        assertEquals(99L, response.id());

        assertTrue(response.claimNumber().startsWith("CLM-"));
        assertEquals(10L, response.policyId());
        assertEquals(request.lossDate(), response.lossDate());
        assertEquals(request.noticeDate(), response.noticeDate());
        assertEquals(request.claimedAmount(), response.claimedAmount());
        assertEquals(ClaimStatus.OPENED, response.status());

        verify(claimRepository).save(any(ClaimEntity.class));
    }

    @Test
    void rejectsClaimForInactivePolicy() {
        PolicyEntity policy = new PolicyEntity(
                "POL-2026-000001",
                null,
                1L,
                1L,
                Set.of(),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                PolicyStatus.SUSPENDED
        );
        ReflectionTestUtils.setField(policy, "id", 10L);

        CreateClaimRequest request = new CreateClaimRequest(
                10L,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16),
                new BigDecimal("1500.00")
        );

        when(policyRepository.findById(10L))
                .thenReturn(Optional.of(policy));

        InvalidClaimRequestException invalidClaimRequestException = assertThrows(
                InvalidClaimRequestException.class,
                () -> claimService.openClaim(request)
        );

        assertEquals(
                "claim can be opened only on an active policy",
                invalidClaimRequestException.getMessage()
        );

        verifyNoInteractions(claimRepository);
    }

    @Test
    void rejectsLossDateOutsidePolicyPeriod() {
        PolicyEntity policy = new PolicyEntity(
                "POL-2026-000001",
                null,
                1L,
                1L,
                Set.of(),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                PolicyStatus.ACTIVE
        );
        ReflectionTestUtils.setField(policy, "id", 10L);

        CreateClaimRequest request = new CreateClaimRequest(
                10L,
                LocalDate.of(2027, 1, 1),
                LocalDate.of(2027, 1, 2),
                new BigDecimal("1500.00")
        );

        when(policyRepository.findById(10L))
                .thenReturn(Optional.of(policy));

        InvalidClaimRequestException exception = assertThrows(
                InvalidClaimRequestException.class,
                () -> claimService.openClaim(request)
        );

        assertEquals(
                "lossDate must be inside the policy period",
                exception.getMessage()
        );

        verifyNoInteractions(claimRepository);
    }

    @Test
    void returnsClaimWhenClaimExists() {
        PolicyEntity policy = new PolicyEntity(
                "POL-2026-000001",
                null,
                1L,
                1L,
                Set.of(),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                PolicyStatus.ACTIVE
        );
        ReflectionTestUtils.setField(policy, "id", 10L);

        ClaimEntity claim = new ClaimEntity(
                "CLM-2026-000001",
                policy,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16),
                new BigDecimal("1500.00")
        );
        ReflectionTestUtils.setField(claim, "id", 99L);

        when(claimRepository.findById(99L))
                .thenReturn(Optional.of(claim));

        ClaimResponse response = claimService.getClaimById(99L);

        assertEquals(99L, response.id());
        assertEquals("CLM-2026-000001", response.claimNumber());
        assertEquals(10L, response.policyId());
        assertEquals(LocalDate.of(2026, 6, 15), response.lossDate());
        assertEquals(LocalDate.of(2026, 6, 16), response.noticeDate());
        assertEquals(new BigDecimal("1500.00"), response.claimedAmount());
        assertEquals(ClaimStatus.OPENED, response.status());
    }

    @Test
    void throwsWhenClaimDoesNotExist() {
        when(claimRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> claimService.getClaimById(99L)
        );

        assertEquals("claim not found", exception.getMessage());
    }

    @Test
    void returnsClaimsForExistingPolicy() {
        PolicyEntity policy = new PolicyEntity(
                "POL-2026-000001",
                null,
                1L,
                1L,
                Set.of(),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                PolicyStatus.ACTIVE
        );
        ReflectionTestUtils.setField(policy, "id", 10L);

        ClaimEntity firstClaim = new ClaimEntity(
                "CLM-2026-000001",
                policy,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16),
                new BigDecimal("1500.00")
        );
        ReflectionTestUtils.setField(firstClaim, "id", 99L);

        ClaimEntity secondClaim = new ClaimEntity(
                "CLM-2026-000002",
                policy,
                LocalDate.of(2026, 7, 10),
                LocalDate.of(2026, 7, 11),
                new BigDecimal("800.00")
        );
        ReflectionTestUtils.setField(secondClaim, "id", 100L);

        when(policyRepository.existsById(10L))
                .thenReturn(true);

        when(claimRepository.findByPolicyIdOrderByIdAsc(10L))
                .thenReturn(List.of(firstClaim, secondClaim));

        List<ClaimResponse> responses = claimService.getClaimsByPolicyId(10L);

        assertEquals(2, responses.size());
        assertEquals(99L, responses.get(0).id());
        assertEquals("CLM-2026-000001", responses.get(0).claimNumber());
        assertEquals(100L, responses.get(1).id());
        assertEquals("CLM-2026-000002", responses.get(1).claimNumber());
    }

    @Test
    void throwsWhenListingClaimsForMissingPolicy() {
        when(policyRepository.existsById(10L))
                .thenReturn(false);

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> claimService.getClaimsByPolicyId(10L)
        );

        assertEquals("policy not found", exception.getMessage());

        verifyNoInteractions(claimRepository);
    }
}
