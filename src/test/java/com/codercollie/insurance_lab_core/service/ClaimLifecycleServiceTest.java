package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.domain.ClaimStatus;
import com.codercollie.insurance_lab_core.dto.claim.ClaimResponse;
import com.codercollie.insurance_lab_core.dto.claim.ReserveClaimRequest;
import com.codercollie.insurance_lab_core.dto.claim.SettleClaimRequest;
import com.codercollie.insurance_lab_core.dto.claim_movement.ClaimMovementResponse;
import com.codercollie.insurance_lab_core.exception.InvalidClaimRequestException;
import com.codercollie.insurance_lab_core.exception.ResourceNotFoundException;
import com.codercollie.insurance_lab_core.mapper.ClaimMapper;
import com.codercollie.insurance_lab_core.persistence.entity.ClaimEntity;
import com.codercollie.insurance_lab_core.persistence.entity.ClaimMovementEntity;
import com.codercollie.insurance_lab_core.persistence.entity.PolicyEntity;
import com.codercollie.insurance_lab_core.repository.ClaimMovementRepository;
import com.codercollie.insurance_lab_core.repository.ClaimRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimLifecycleServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ClaimMovementRepository claimMovementRepository;

    private ClaimLifecycleService claimLifecycleService;

    @BeforeEach
    void setUp() {
        claimLifecycleService = new ClaimLifecycleService(
                claimRepository,
                claimMovementRepository,
                new ClaimMapper()
        );
    }

    @Test
    void throwsWhenReservingMissingClaim() {
        when(claimRepository.findById(99L))
                .thenReturn(Optional.empty());

        ReserveClaimRequest request = new ReserveClaimRequest(
                new BigDecimal("3000.00")
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> claimLifecycleService.reserveClaim(99L, request)
        );
    }

    @Test
    void reservesOpenedClaimAndCreatesMovement() {
        PolicyEntity policy = mock(PolicyEntity.class);
        when(policy.getId()).thenReturn(10L);

        ClaimEntity claim = new ClaimEntity(
                "CLM-2026-000001",
                policy,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16),
                new BigDecimal("5000.00")
        );
        ReflectionTestUtils.setField(claim, "id", 99L);

        when(claimRepository.findById(99L))
                .thenReturn(Optional.of(claim));

        ReserveClaimRequest reserveRequest = new ReserveClaimRequest(
                new BigDecimal("3000.00")
        );

        ClaimResponse response = claimLifecycleService.reserveClaim(
                99L,
                reserveRequest
        );

        assertEquals(ClaimStatus.RESERVED, response.status());
        assertEquals(
                new BigDecimal("3000.00"),
                response.reservedAmount()
        );
        assertEquals(BigDecimal.ZERO, response.settledAmount());

        ArgumentCaptor<ClaimMovementEntity> movementCaptor =
                ArgumentCaptor.forClass(ClaimMovementEntity.class);
        verify(claimMovementRepository)
                .save(movementCaptor.capture());
        ClaimMovementEntity claimMovement = movementCaptor.getValue();

        assertSame(claim, claimMovement.getClaim());
        assertEquals(
                ClaimStatus.RESERVED,
                claimMovement.getStatus()
        );
        assertEquals(
                new BigDecimal("3000.00"),
                claimMovement.getAmount()
        );
        assertEquals(
                "Reserve set",
                claimMovement.getNote()
        );
        assertNotNull(claimMovement.getCreatedAt());
    }

    @Test
    void rejectsReserveForRejectedClaim() {
        PolicyEntity policy = mock(PolicyEntity.class);

        ClaimEntity claim = new ClaimEntity(
                "CLM-2026-000001",
                policy,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16),
                new BigDecimal("5000.00")
        );
        ReflectionTestUtils.setField(claim, "id", 99L);
        ReflectionTestUtils.setField(
                claim,
                "status",
                ClaimStatus.REJECTED
        );

        when(claimRepository.findById(99L))
                .thenReturn(Optional.of(claim));

        ReserveClaimRequest reserveRequest = new ReserveClaimRequest(
                new BigDecimal("3000.00")
        );

        InvalidClaimRequestException exception = assertThrows(
                InvalidClaimRequestException.class,
                () -> claimLifecycleService.reserveClaim(
                        99L,
                        reserveRequest
                )
        );
        assertEquals(
                "only an opened claim can be reserved",
                exception.getMessage()
        );
        assertEquals(ClaimStatus.REJECTED, claim.getStatus());
        assertEquals(BigDecimal.ZERO, claim.getReservedAmount());

        verifyNoInteractions(claimMovementRepository);
    }

    @Test
    void rejectsReserveAboveFakeMaximumPayout() {
        PolicyEntity policy = mock(PolicyEntity.class);

        ClaimEntity claim = new ClaimEntity(
                "CLM-2026-000001",
                policy,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16),
                new BigDecimal("100000.00")
        );

        when(claimRepository.findById(99L))
                .thenReturn(Optional.of(claim));

        ReserveClaimRequest request = new ReserveClaimRequest(
                new BigDecimal("50000.01")
        );

        InvalidClaimRequestException exception = assertThrows(
                InvalidClaimRequestException.class,
                () -> claimLifecycleService.reserveClaim(
                        99L,
                        request
                )
        );

        assertEquals(
                "reserve amount must not exceed 50000.00",
                exception.getMessage()
        );
        assertEquals(ClaimStatus.OPENED, claim.getStatus());
        assertEquals(BigDecimal.ZERO, claim.getReservedAmount());

        verifyNoInteractions(claimMovementRepository);
    }

    @Test
    void settlesReservedClaimAndCreatesMovement() {
        PolicyEntity policy = mock(PolicyEntity.class);
        ClaimEntity claim = new ClaimEntity(
                "CLM-2026-000001",
                policy,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16),
                new BigDecimal("10000.00")
        );

        claim.reserve(new BigDecimal("3000.00"));

        when(claimRepository.findById(99L))
                .thenReturn(Optional.of(claim));

        SettleClaimRequest settleRequest = new SettleClaimRequest(
                new BigDecimal("2500.00")
        );

        ClaimResponse response = claimLifecycleService.settleClaim(
                99L,
                settleRequest
        );

        assertEquals(ClaimStatus.SETTLED, response.status());
        assertEquals(new BigDecimal("3000.00"), response.reservedAmount());
        assertEquals(new BigDecimal("2500.00"), response.settledAmount());

        ArgumentCaptor<ClaimMovementEntity> movementCaptor =
                ArgumentCaptor.forClass(ClaimMovementEntity.class);
        verify(claimMovementRepository)
                .save(movementCaptor.capture());

        ClaimMovementEntity movement = movementCaptor.getValue();

        assertSame(claim, movement.getClaim());
        assertEquals(ClaimStatus.SETTLED, movement.getStatus());
        assertEquals(new BigDecimal("2500.00"), movement.getAmount());
        assertEquals("Claim settled", movement.getNote());
        assertNotNull(movement.getCreatedAt());
    }

    @Test
    void rejectsSettlementForOpenedClaim() {
        PolicyEntity policy = mock(PolicyEntity.class);

        ClaimEntity claim = new ClaimEntity(
                "CLM-2026-000001",
                policy,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16),
                new BigDecimal("10000.00")
        );

        when(claimRepository.findById(99L))
                .thenReturn(Optional.of(claim));

        SettleClaimRequest settleRequest = new SettleClaimRequest(
                new BigDecimal("2500.00")
        );

        InvalidClaimRequestException exception = assertThrows(
                InvalidClaimRequestException.class,
                () -> claimLifecycleService.settleClaim(
                        99L,
                        settleRequest
                )
        );

        assertEquals(
                "only a reserved claim can be settled",
                exception.getMessage()
        );
        assertEquals(ClaimStatus.OPENED, claim.getStatus());
        assertEquals(BigDecimal.ZERO, claim.getSettledAmount());

        verifyNoInteractions(claimMovementRepository);
    }

    @Test
    void rejectsSettlementAboveReservedAmount() {
        PolicyEntity policy = mock(PolicyEntity.class);

        ClaimEntity claim = new ClaimEntity(
                "CLM-2026-000001",
                policy,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16),
                new BigDecimal("10000.00")
        );

        claim.reserve(new BigDecimal("3000.00"));

        when(claimRepository.findById(99L))
                .thenReturn(Optional.of(claim));

        SettleClaimRequest settleRequest = new SettleClaimRequest(
                new BigDecimal("3000.01")
        );

        InvalidClaimRequestException exception = assertThrows(
                InvalidClaimRequestException.class,
                () -> claimLifecycleService.settleClaim(
                        99L,
                        settleRequest
                )
        );

        assertEquals(
                "settlement amount must not exceed reserved amount",
                exception.getMessage()
        );
        assertEquals(ClaimStatus.RESERVED, claim.getStatus());
        assertEquals(BigDecimal.ZERO, claim.getSettledAmount());

        verifyNoInteractions(claimMovementRepository);
    }

    @Test
    void rejectsOpenedClaimAndCreatesMovement() {
        PolicyEntity policy = mock(PolicyEntity.class);

        ClaimEntity claim = new ClaimEntity(
                "CLM-2026-000001",
                policy,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16),
                new BigDecimal("10000.00")
        );

        when(claimRepository.findById(99L))
                .thenReturn(Optional.of(claim));

        ClaimResponse response = claimLifecycleService.rejectClaim(99L);

        assertEquals(ClaimStatus.REJECTED, response.status());
        assertEquals(BigDecimal.ZERO, response.reservedAmount());
        assertEquals(BigDecimal.ZERO, response.settledAmount());

        ArgumentCaptor<ClaimMovementEntity> movementCaptor =
                ArgumentCaptor.forClass(ClaimMovementEntity.class);

        verify(claimMovementRepository)
                .save(movementCaptor.capture());

        ClaimMovementEntity movement = movementCaptor.getValue();

        assertSame(claim, movement.getClaim());
        assertEquals(ClaimStatus.REJECTED, movement.getStatus());
        assertEquals(BigDecimal.ZERO, movement.getAmount());
        assertEquals("Claim rejected", movement.getNote());
        assertNotNull(movement.getCreatedAt());
    }

    @Test
    void rejectsRejectionForReservedClaim() {
        PolicyEntity policy = mock(PolicyEntity.class);

        ClaimEntity claim = new ClaimEntity(
                "CLM-2026-000001",
                policy,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16),
                new BigDecimal("10000.00")
        );

        claim.reserve(new BigDecimal("3000.00"));

        when(claimRepository.findById(99L))
                .thenReturn(Optional.of(claim));

        InvalidClaimRequestException exception = assertThrows(
                InvalidClaimRequestException.class,
                () -> claimLifecycleService.rejectClaim(99L)
        );

        assertEquals(
                "only an opened claim can be rejected",
                exception.getMessage()
        );
        assertEquals(ClaimStatus.RESERVED, claim.getStatus());
        assertEquals(new BigDecimal("3000.00"), claim.getReservedAmount());
        assertEquals(BigDecimal.ZERO, claim.getSettledAmount());

        verifyNoInteractions(claimMovementRepository);
    }

    @Test
    void returnsClaimMovementsOrderedByCreation() {
        PolicyEntity policy = mock(PolicyEntity.class);

        ClaimEntity claim = new ClaimEntity(
                "CLM-2026-000001",
                policy,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16),
                new BigDecimal("10000.00")
        );

        when(claimRepository.findById(99L))
                .thenReturn(Optional.of(claim));

        ClaimMovementEntity firstMovement = new ClaimMovementEntity(
                claim,
                ClaimStatus.OPENED,
                BigDecimal.ZERO,
                "Claim opened",
                Instant.parse("2026-06-16T10:00:00Z")
        );

        ClaimMovementEntity secondMovement = new ClaimMovementEntity(
                claim,
                ClaimStatus.RESERVED,
                new BigDecimal("3000.00"),
                "Reserve set",
                Instant.parse("2026-06-16T11:00:00Z")
        );

        when(claimMovementRepository.findByClaimIdOrderByIdAsc(99L))
                .thenReturn(List.of(firstMovement, secondMovement));

        List<ClaimMovementResponse> response =
                claimLifecycleService.getClaimMovements(99L);

        assertEquals(2, response.size());
        assertEquals(ClaimStatus.OPENED, response.get(0).status());
        assertEquals("Claim opened", response.get(0).note());
        assertEquals(ClaimStatus.RESERVED, response.get(1).status());
        assertEquals(new BigDecimal("3000.00"), response.get(1).amount());

        verify(claimRepository).findById(99L);
        verify(claimMovementRepository).findByClaimIdOrderByIdAsc(99L);
    }
}
