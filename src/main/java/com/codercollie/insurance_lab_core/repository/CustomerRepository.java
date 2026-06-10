package com.codercollie.insurance_lab_core.repository;

import com.codercollie.insurance_lab_core.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
}
