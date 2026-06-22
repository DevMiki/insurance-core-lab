package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.dto.claim.ReserveClaimRequest;
import com.codercollie.insurance_lab_core.exception.ResourceNotFoundException;
import com.codercollie.insurance_lab_core.mapper.ClaimMapper;
import com.codercollie.insurance_lab_core.repository.ClaimMovementRepository;
import com.codercollie.insurance_lab_core.repository.ClaimRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
}
