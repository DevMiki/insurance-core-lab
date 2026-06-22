package com.codercollie.insurance_lab_core.repository;

import com.codercollie.insurance_lab_core.persistence.entity.ClaimMovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimMovementRepository extends JpaRepository<ClaimMovementEntity, Long> {
    List<ClaimMovementEntity> findByClaimIdOrderByIdAsc(Long claimId);
}
