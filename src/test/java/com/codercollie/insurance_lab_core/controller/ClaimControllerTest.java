package com.codercollie.insurance_lab_core.controller;

import com.codercollie.insurance_lab_core.domain.ClaimStatus;
import com.codercollie.insurance_lab_core.dto.claim.ClaimResponse;
import com.codercollie.insurance_lab_core.dto.claim.CreateClaimRequest;
import com.codercollie.insurance_lab_core.dto.claim.ReserveClaimRequest;
import com.codercollie.insurance_lab_core.exception.ResourceNotFoundException;
import com.codercollie.insurance_lab_core.service.ClaimLifecycleService;
import com.codercollie.insurance_lab_core.service.ClaimService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClaimController.class)
class ClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClaimService claimService;

    @MockitoBean
    private ClaimLifecycleService claimLifecycleService;

    @Test
    void returnsCreatedClaimWhenRequestIsValid() throws Exception {
        CreateClaimRequest request = new CreateClaimRequest(
                10L,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16),
                new BigDecimal("1500.00")
        );

        ClaimResponse response = new ClaimResponse(
                99L,
                "CLM-2026-000001",
                10L,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16),
                new BigDecimal("1500.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                ClaimStatus.OPENED
        );

        when(claimService.openClaim(request))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.claimNumber", is("CLM-2026-000001")))
                .andExpect(jsonPath("$.policyId", is(10)))
                .andExpect(jsonPath("$.lossDate", is("2026-06-15")))
                .andExpect(jsonPath("$.noticeDate", is("2026-06-16")))
                .andExpect(jsonPath("$.claimedAmount", is(1500.00)))
                .andExpect(jsonPath("$.status", is("OPENED")))
                .andExpect(jsonPath("$.reservedAmount", is(0)))
                .andExpect(jsonPath("$.settledAmount", is(0)));

        verify(claimService).openClaim(request);
    }

    @Test
    void rejectsZeroClaimedAmount() throws Exception {
        CreateClaimRequest request = new CreateClaimRequest(
                10L,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16),
                BigDecimal.ZERO
        );

        mockMvc.perform(post("/api/v1/claims")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath(
                        "$.message",
                        is("claimedAmount must be greater than zero")
                ));

        verify(claimService, never())
                .openClaim(any(CreateClaimRequest.class));
    }

    @Test
    void returnsClaimWhenClaimExists() throws Exception {
        ClaimResponse response = new ClaimResponse(
                99L,
                "CLM-2026-000001",
                10L,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16),
                new BigDecimal("1500.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                ClaimStatus.OPENED
        );

        when(claimService.getClaimById(99L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/claims/{id}", 99L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath(
                        "$.claimNumber",
                        is("CLM-2026-000001")
                ))
                .andExpect(jsonPath("$.policyId", is(10)))
                .andExpect(jsonPath("$.lossDate", is("2026-06-15")))
                .andExpect(jsonPath("$.noticeDate", is("2026-06-16")))
                .andExpect(jsonPath("$.claimedAmount", is(1500.00)))
                .andExpect(jsonPath("$.status", is("OPENED")));

        verify(claimService).getClaimById(99L);
    }

    @Test
    void returnsClaimsBelongingToPolicy() throws Exception {
        ClaimResponse response = new ClaimResponse(
                99L,
                "CLM-2026-000001",
                10L,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16),
                new BigDecimal("1500.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                ClaimStatus.OPENED
        );

        when(claimService.getClaimsByPolicyId(10L))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/policies/{policyId}/claims", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(99)))
                .andExpect(jsonPath(
                        "$[0].claimNumber",
                        is("CLM-2026-000001")
                ))
                .andExpect(jsonPath("$[0].policyId", is(10)))
                .andExpect(jsonPath("$[0].status", is("OPENED")));

        verify(claimService).getClaimsByPolicyId(10L);
    }

    @Test
    void returnsNotFoundWhenClaimDoesNotExist() throws Exception {
        when(claimService.getClaimById(999L))
                .thenThrow(new ResourceNotFoundException("claim not found"));

        mockMvc.perform(get("/api/v1/claims/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("claim not found")));

        verify(claimService).getClaimById(999L);
    }

    @Test
    void returnsNotFoundWhenListingClaimsForMissingPolicy() throws Exception {
        when(claimService.getClaimsByPolicyId(999L))
                .thenThrow(new ResourceNotFoundException("policy not found"));

        mockMvc.perform(get(
                        "/api/v1/policies/{policyId}/claims",
                        999L
                ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("policy not found")));

        verify(claimService).getClaimsByPolicyId(999L);
    }

    @Test
    void reservesClaimWhenRequestIsValid() throws Exception {
        ReserveClaimRequest request = new ReserveClaimRequest(
                new BigDecimal("3000.00")
        );

        ClaimResponse response = new ClaimResponse(
                99L,
                "CLM-2026-000001",
                10L,
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16),
                new BigDecimal("5000.00"),
                new BigDecimal("3000.00"),
                BigDecimal.ZERO,
                ClaimStatus.RESERVED
        );

        when(claimLifecycleService.reserveClaim(99L, request))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/claims/{claimId}/reserve",
                        99L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.reservedAmount", is(3000.00)))
                .andExpect(jsonPath("$.settledAmount", is(0)))
                .andExpect(jsonPath("$.status", is("RESERVED")));

        verify(claimLifecycleService).reserveClaim(99L, request);
    }
}
