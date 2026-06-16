package com.codercollie.insurance_lab_core.repository;

import com.codercollie.insurance_lab_core.persistence.entity.PremiumEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PremiumRepository extends JpaRepository<PremiumEntity, Long> {
}
