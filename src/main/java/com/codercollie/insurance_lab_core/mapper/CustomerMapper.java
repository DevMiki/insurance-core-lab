package com.codercollie.insurance_lab_core.mapper;

import com.codercollie.insurance_lab_core.dto.customer.CreateCustomerRequest;
import com.codercollie.insurance_lab_core.dto.customer.CustomerResponse;
import com.codercollie.insurance_lab_core.persistence.entity.CustomerEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerEntity toEntity(CreateCustomerRequest request) {
        return new CustomerEntity(
                request.customerCode(),
                request.fullName()
        );
    }

    public CustomerResponse toResponse(CustomerEntity entity) {
        return new CustomerResponse(
                entity.getId(),
                entity.getCustomerCode(),
                entity.getFullName()
        );
    }

}
