package com.codercollie.insurance_lab_core.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_code", nullable = false, unique = true, length = 50)
    private String customerCode;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    protected CustomerEntity() {
    }

    public CustomerEntity(String customerCode, String fullName) {
        this.customerCode = customerCode;
        this.fullName = fullName;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public String getFullName() {
        return fullName;
    }
}
