package com.codercollie.insurance_lab_core.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "quotes")
public class QuoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "quote_coverages",
            joinColumns = @JoinColumn(name = "quote_id")
    )
    @Column(name = "coverage_id", nullable = false)
    private List<Long> coverageIds = new ArrayList<>();

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "net_premium", nullable = false, precision = 12, scale = 2)
    private BigDecimal netPremium;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected QuoteEntity() {
    }

    public QuoteEntity(
            Long productId,
            List<Long> coverageIds,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal netPremium,
            BigDecimal taxAmount,
            BigDecimal totalAmount,
            Instant createdAt
    ) {
        this.productId = productId;
        this.coverageIds = new ArrayList<>(coverageIds);
        this.startDate = startDate;
        this.endDate = endDate;
        this.netPremium = netPremium;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
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

    public BigDecimal getNetPremium() {
        return netPremium;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
