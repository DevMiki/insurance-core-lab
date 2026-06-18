package com.codercollie.insurance_lab_core.repository;

import com.codercollie.insurance_lab_core.persistence.entity.ClaimEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimRepository extends JpaRepository<ClaimEntity, Long> {
    List<ClaimEntity> findByPolicyIdOrderByIdAsc(Long policyId);
}
