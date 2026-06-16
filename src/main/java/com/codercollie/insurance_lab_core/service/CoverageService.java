package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.dto.coverage.CoverageResponse;
import com.codercollie.insurance_lab_core.dto.coverage.CreateCoverageRequest;
import com.codercollie.insurance_lab_core.exception.ResourceNotFoundException;
import com.codercollie.insurance_lab_core.mapper.CoverageMapper;
import com.codercollie.insurance_lab_core.persistence.entity.CoverageEntity;
import com.codercollie.insurance_lab_core.persistence.entity.ProductEntity;
import com.codercollie.insurance_lab_core.repository.CoverageRepository;
import com.codercollie.insurance_lab_core.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CoverageService {

    private final ProductRepository productRepository;
    private final CoverageRepository coverageRepository;
    private final CoverageMapper coverageMapper;

    public CoverageService(
            ProductRepository productRepository,
            CoverageRepository coverageRepository,
            CoverageMapper coverageMapper
    ) {
        this.productRepository = productRepository;
        this.coverageRepository = coverageRepository;
        this.coverageMapper = coverageMapper;
    }

    public CoverageResponse addCoverageToProduct(Long productId, CreateCoverageRequest request) {
        final ProductEntity product = findProductById(productId);
        final CoverageEntity coverage = coverageMapper.toEntity(request);

        product.addCoverage(coverage);
        final CoverageEntity savedCoverage = coverageRepository.save(coverage);

        return coverageMapper.toResponse(savedCoverage);
    }

    @Transactional(readOnly = true)
    public List<CoverageResponse> getCoveragesByProductId(Long productId) {
        return coverageRepository.findByProduct_Id(productId)
                .stream()
                .map(coverageMapper::toResponse)
                .toList();
    }

    private ProductEntity findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("product not found"));
    }
}
