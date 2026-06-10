package com.codercollie.insurance_lab_core.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code", nullable = false, unique = true, length = 50)
    private String productCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CoverageEntity> coverages = new LinkedHashSet<>();

    protected ProductEntity() {
    }

    public ProductEntity(String productCode, String name) {
        this.productCode = productCode;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getName() {
        return name;
    }

    public Set<CoverageEntity> getCoverages() {
        return Collections.unmodifiableSet(coverages);
    }

    public void addCoverage(CoverageEntity coverage) {
        coverages.add(coverage);
        coverage.assignToProduct(this);
    }
}
