package com.codercollie.insurance_lab_core.persistence.entity;

import com.codercollie.insurance_lab_core.domain.ClaimStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "claims")
public class ClaimEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_number", nullable = false, unique = true, length = 50)
    private String claimNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private PolicyEntity policy;

    @Column(name = "loss_date", nullable = false)
    private LocalDate lossDate;

    @Column(name = "notice_date", nullable = false)
    private LocalDate noticeDate;

    @Column(name = "claimed_amount", nullable = false,
            precision = 12, scale = 2)
    private BigDecimal claimedAmount;

    @Column(
            name = "reserved_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal reservedAmount;

    @Column(
            name = "settled_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal settledAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ClaimStatus status;

    protected ClaimEntity() {
    }

    public ClaimEntity(
            String claimNumber,
            PolicyEntity policy,
            LocalDate lossDate,
            LocalDate noticeDate,
            BigDecimal claimedAmount
    ) {
        this.claimNumber = claimNumber;
        this.policy = policy;
        this.lossDate = lossDate;
        this.noticeDate = noticeDate;
        this.claimedAmount = claimedAmount;
        this.reservedAmount = BigDecimal.ZERO;
        this.settledAmount = BigDecimal.ZERO;
        this.status = ClaimStatus.OPENED;
    }

    public Long getId() {
        return id;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public PolicyEntity getPolicy() {
        return policy;
    }

    public LocalDate getLossDate() {
        return lossDate;
    }

    public LocalDate getNoticeDate() {
        return noticeDate;
    }

    public BigDecimal getClaimedAmount() {
        return claimedAmount;
    }

    public BigDecimal getReservedAmount() {
        return reservedAmount;
    }

    public BigDecimal getSettledAmount() {
        return settledAmount;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void reserve(BigDecimal amount) {
        this.reservedAmount = amount;
        this.status = ClaimStatus.RESERVED;
    }

    public void settle(BigDecimal amount) {
        this.settledAmount = amount;
        this.status = ClaimStatus.SETTLED;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClaimEntity that = (ClaimEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
