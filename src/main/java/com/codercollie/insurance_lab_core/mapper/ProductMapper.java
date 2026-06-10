package com.codercollie.insurance_lab_core.mapper;

import com.codercollie.insurance_lab_core.dto.product.CreateProductRequest;
import com.codercollie.insurance_lab_core.dto.product.ProductResponse;
import com.codercollie.insurance_lab_core.persistence.entity.ProductEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductEntity toEntity(CreateProductRequest request) {
        return new ProductEntity(
                request.productCode(),
                request.name()
        );
    }

    public ProductResponse toResponse(ProductEntity entity) {
        return new ProductResponse(
                entity.getId(),
                entity.getProductCode(),
                entity.getName()
        );
    }
}
