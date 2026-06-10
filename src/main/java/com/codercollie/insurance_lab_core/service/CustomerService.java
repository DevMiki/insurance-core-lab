package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.dto.customer.CreateCustomerRequest;
import com.codercollie.insurance_lab_core.dto.customer.CustomerResponse;
import com.codercollie.insurance_lab_core.mapper.CustomerMapper;
import com.codercollie.insurance_lab_core.persistence.entity.CustomerEntity;
import com.codercollie.insurance_lab_core.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CustomerService {

    private final CustomerMapper customerMapper;
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerMapper customerMapper, CustomerRepository customerRepository) {
        this.customerMapper = customerMapper;
        this.customerRepository = customerRepository;
    }

    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        final CustomerEntity customer = customerMapper.toEntity(request);
        final CustomerEntity savedCustomer = customerRepository.save(customer);
        return customerMapper.toResponse(savedCustomer);
    }

}
