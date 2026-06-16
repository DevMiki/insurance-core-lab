package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.dto.coverage.CoverageResponse;
import com.codercollie.insurance_lab_core.dto.coverage.CreateCoverageRequest;
import com.codercollie.insurance_lab_core.exception.ResourceNotFoundException;
import com.codercollie.insurance_lab_core.mapper.CoverageMapper;
import com.codercollie.insurance_lab_core.persistence.entity.CoverageEntity;
import com.codercollie.insurance_lab_core.persistence.entity.ProductEntity;
import com.codercollie.insurance_lab_core.repository.CoverageRepository;
import com.codercollie.insurance_lab_core.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoverageServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CoverageRepository coverageRepository;

    private CoverageService coverageService;

    @BeforeEach
    void setUp() {
        coverageService = new CoverageService(
                productRepository,
                coverageRepository,
                new CoverageMapper()
        );
    }

    @Test
    void addsCoverageWhenProductExists() {
        final ProductEntity product = productWithId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(coverageRepository.save(any(CoverageEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        final CoverageResponse response = coverageService.addCoverageToProduct(
                1L,
                new CreateCoverageRequest(
                        "FIRE",
                        "Fire coverage",
                        "Protects the insured home against fire damage",
                        new BigDecimal("120.00")
                )
        );

        final ArgumentCaptor<CoverageEntity> coverageCaptor = ArgumentCaptor.forClass(CoverageEntity.class);
        verify(coverageRepository).save(coverageCaptor.capture());

        final CoverageEntity savedCoverage = coverageCaptor.getValue();
        assertSame(product, savedCoverage.getProduct());
        assertEquals(1L, response.productId());
        assertEquals("FIRE", response.code());
    }

    @Test
    void throwsNotFoundWhenProductDoesNotExist() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> coverageService.addCoverageToProduct(
                        999L,
                        new CreateCoverageRequest(
                                "FIRE",
                                "Fire coverage",
                                "Protects the insured home against fire damage",
                                new BigDecimal("120.00")
                        )
                )
        );

        verify(coverageRepository, never()).save(any(CoverageEntity.class));
    }

    @Test
    void returnsOnlyCoveragesForSelectedProduct() {
        final ProductEntity product = productWithId(1L);
        final CoverageEntity coverage = new CoverageEntity(
                "FIRE",
                "Fire coverage",
                "Protects the insured home against fire damage",
                new BigDecimal("120.00")
        );
        ReflectionTestUtils.setField(coverage, "id", 10L);
        product.addCoverage(coverage);

        when(coverageRepository.findByProduct_Id(1L)).thenReturn(List.of(coverage));

        final List<CoverageResponse> responses = coverageService.getCoveragesByProductId(1L);

        assertEquals(1, responses.size());
        assertEquals(10L, responses.getFirst().id());
        assertEquals(1L, responses.getFirst().productId());
        verify(coverageRepository).findByProduct_Id(1L);
    }

    private ProductEntity productWithId(Long id) {
        final ProductEntity product = new ProductEntity("HOME", "Home Insurance");
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
