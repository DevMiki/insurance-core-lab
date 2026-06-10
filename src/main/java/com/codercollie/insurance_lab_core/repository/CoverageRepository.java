package com.codercollie.insurance_lab_core.repository;

import com.codercollie.insurance_lab_core.persistence.entity.CoverageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoverageRepository extends JpaRepository<CoverageEntity, Long> {

    List<CoverageEntity> findByProduct_Id(Long productId);
}
