package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.domain.ClaimStatus;
import com.codercollie.insurance_lab_core.domain.PolicyStatus;
import com.codercollie.insurance_lab_core.dto.claim.ClaimResponse;
import com.codercollie.insurance_lab_core.dto.claim.CreateClaimRequest;
import com.codercollie.insurance_lab_core.exception.InvalidClaimRequestException;
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
}
