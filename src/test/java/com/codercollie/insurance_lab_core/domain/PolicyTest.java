package com.codercollie.insurance_lab_core.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PolicyTest {

    @Test
    void rejectsMissingPolicyNumber() {
        assertThrows(IllegalArgumentException.class,
                () -> new Policy(
                        null,
                        customer(),
                        product(),
                        List.of(coverage()),
                        premium(),
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31),
                        PolicyStatus.QUOTE
                ));
    }

    @Test
    void rejectsMissingCustomer() {
        assertThrows(NullPointerException.class,
                () -> new Policy(
                        "POL-2026-0001",
                        null,
                        product(),
                        List.of(coverage()),
                        premium(),
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31),
                        PolicyStatus.QUOTE
                ));
    }

    @Test
    void rejectsPolicyWithoutCoverages() {
        assertThrows(IllegalArgumentException.class,
                () -> new Policy(
                        "POL-2026-0001",
                        customer(),
                        product(),
                        List.of(),
                        premium(),
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 12, 31),
                        PolicyStatus.QUOTE
                ));
    }

    @Test
    void rejectsEndDateBeforeStartDate() {
        assertThrows(IllegalArgumentException.class,
                () -> new Policy(
                        "POL-2026-0001",
                        customer(),
                        product(),
                        List.of(coverage()),
                        premium(),
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 1, 31),
                        PolicyStatus.QUOTE
                ));
    }

    @Test
    void protectsCoveragesFromExternalChanges() {
        List<Coverage> coverages = new ArrayList<>();
        coverages.add(coverage());

        Policy policy = new Policy(
                "POL-2026-0001",
                customer(),
                product(),
                coverages,
                premium(),
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                PolicyStatus.QUOTE
        );

        coverages.add(new Coverage("THEFT", "Theft protection", new BigDecimal("50000.00")));

        assertEquals(1, policy.coverages().size());
        assertThrows(UnsupportedOperationException.class,
                () -> policy.coverages().add(coverage()));
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
