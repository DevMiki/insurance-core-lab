package com.codercollie.insurance_lab_core.controller;

import com.codercollie.insurance_lab_core.dto.coverage.CoverageResponse;
import com.codercollie.insurance_lab_core.dto.coverage.CreateCoverageRequest;
import com.codercollie.insurance_lab_core.dto.product.CreateProductRequest;
import com.codercollie.insurance_lab_core.dto.product.ProductResponse;
import com.codercollie.insurance_lab_core.service.CoverageService;
import com.codercollie.insurance_lab_core.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    private final CoverageService coverageService;

    public ProductController(ProductService productService, CoverageService coverageService) {
        this.productService = productService;
        this.coverageService = coverageService;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody CreateProductRequest request) {
        return productService.createProduct(request);
    }

    @PostMapping("/{productId}/coverages")
    @ResponseStatus(HttpStatus.CREATED)
    public CoverageResponse addCoverageToProduct(
            @PathVariable Long productId,
            @Valid @RequestBody CreateCoverageRequest request
    ) {
        return coverageService.addCoverageToProduct(productId, request);
    }

    @GetMapping("/{productId}/coverages")
    public List<CoverageResponse> getCoveragesByProductId(@PathVariable Long productId) {
        return coverageService.getCoveragesByProductId(productId);
    }
}
