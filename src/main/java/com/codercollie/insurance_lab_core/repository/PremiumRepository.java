package com.codercollie.insurance_lab_core.repository;

import com.codercollie.insurance_lab_core.persistence.entity.PremiumEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PremiumRepository extends JpaRepository<PremiumEntity, Long> {

    Optional<PremiumEntity> findByPolicyId(Long policyId);
}
