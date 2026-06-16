package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.dto.product.CreateProductRequest;
import com.codercollie.insurance_lab_core.dto.product.ProductResponse;
import com.codercollie.insurance_lab_core.exception.ResourceNotFoundException;
import com.codercollie.insurance_lab_core.mapper.ProductMapper;
import com.codercollie.insurance_lab_core.persistence.entity.ProductEntity;
import com.codercollie.insurance_lab_core.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        final ProductEntity productEntity = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("product not found"));

        return productMapper.toResponse(productEntity);
    }

    public ProductResponse createProduct(CreateProductRequest request) {
        final ProductEntity entity = productMapper.toEntity(request);
        final ProductEntity savedEntity = productRepository.save(entity);
        return productMapper.toResponse(savedEntity);
    }
}
