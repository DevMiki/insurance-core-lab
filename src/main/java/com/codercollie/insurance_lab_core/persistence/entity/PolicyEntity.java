package com.codercollie.insurance_lab_core.persistence.entity;

import com.codercollie.insurance_lab_core.domain.PolicyStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "policies")
public class PolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_number", nullable = false, unique = true, length = 50)
    private String policyNumber;

    @Column(name = "quote_id", unique = true)
    private Long quoteId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "policy_coverage",
            joinColumns = @JoinColumn(name = "policy_id")
    )
    @Column(name = "coverage_id", nullable = false)
    private List<Long> coverageIds = new ArrayList<>();

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PolicyStatus status;

    protected PolicyEntity() {
    }

    public PolicyEntity(
            String policyNumber,
            Long quoteId,
            Long customerId,
            Long productId,
            List<Long> coverageIds,
            LocalDate startDate,
            LocalDate endDate,
            PolicyStatus status
    ) {
        this.policyNumber = policyNumber;
        this.quoteId = quoteId;
        this.customerId = customerId;
        this.productId = productId;
        this.coverageIds = new ArrayList<>(coverageIds);
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public Long getQuoteId() {
        return quoteId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getProductId() {
        return productId;
    }

    public List<Long> getCoverageIds() {
        return Collections.unmodifiableList(coverageIds);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public PolicyStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PolicyEntity that = (PolicyEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
