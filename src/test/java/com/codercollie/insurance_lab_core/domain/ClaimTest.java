package com.codercollie.insurance_lab_core.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClaimTest {

    @Test
    void rejectsMissingClaimNumber() {
        assertThrows(IllegalArgumentException.class,
                () -> new Claim(
                        null,
                        policyWithStatus(PolicyStatus.ACTIVE),
                        LocalDate.of(2026, 6, 15),
                        new BigDecimal("1500.00"),
                        ClaimStatus.OPENED
                ));
    }

    @Test
    void rejectsMissingPolicy() {
        assertThrows(NullPointerException.class,
                () -> new Claim(
                        "CLM-2026-0001",
                        null,
                        LocalDate.of(2026, 6, 15),
                        new BigDecimal("1500.00"),
                        ClaimStatus.OPENED
                ));
    }

    @Test
    void rejectsMissingClaimedAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new Claim(
                        "CLM-2026-0001",
                        policyWithStatus(PolicyStatus.ACTIVE),
                        LocalDate.of(2026, 6, 15),
                        null,
                        ClaimStatus.OPENED
                ));
    }

    @Test
    void rejectsZeroClaimedAmount() {
        assertThrows(IllegalArgumentException.class,
                () -> new Claim(
                        "CLM-2026-0001",
                        policyWithStatus(PolicyStatus.ACTIVE),
                        LocalDate.of(2026, 6, 15),
                        BigDecimal.ZERO,
                        ClaimStatus.OPENED
                ));
    }

    @Test
    void rejectsClaimOnInactivePolicy() {
        assertThrows(IllegalArgumentException.class,
                () -> new Claim(
                        "CLM-2026-0001",
                        policyWithStatus(PolicyStatus.SUSPENDED),
                        LocalDate.of(2026, 6, 15),
                        new BigDecimal("1500.00"),
                        ClaimStatus.OPENED
                ));
    }

    @Test
    void rejectsLossDateOutsidePolicyPeriod() {
        assertThrows(IllegalArgumentException.class,
                () -> new Claim(
                        "CLM-2026-0001",
                        policyWithStatus(PolicyStatus.ACTIVE),
                        LocalDate.of(2027, 1, 1),
                        new BigDecimal("1500.00"),
                        ClaimStatus.OPENED
                ));
    }

    @Test
    void createsClaimOnActivePolicy() {
        Claim claim = new Claim(
                "CLM-2026-0001",
                policyWithStatus(PolicyStatus.ACTIVE),
                LocalDate.of(2026, 6, 15),
                new BigDecimal("1500.00"),
                ClaimStatus.OPENED
        );

        assertEquals(ClaimStatus.OPENED, claim.status());
    }

    private static Policy policyWithStatus(PolicyStatus status) {
        return new Policy(
                "POL-2026-0001",
                customer(),
                product(),
                List.of(coverage()),
                premium(),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                status
        );
    }

    private static Customer customer() {
        return new Customer("CUS-001", "Mario Rossi");
    }

    private static Product product() {
        return new Product("HOME", "Home Insurance");
    }

    private static Coverage coverage() {
        return new Coverage("FIRE", "Fire damage", new BigDecimal("100000.00"));
    }

    private static Premium premium() {
        return new Premium(new BigDecimal("250.00"), LocalDate.of(2026, 1, 1));
    }
}
