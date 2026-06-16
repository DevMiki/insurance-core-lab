package com.codercollie.insurance_lab_core.service;

import com.codercollie.insurance_lab_core.dto.customer.CreateCustomerRequest;
import com.codercollie.insurance_lab_core.dto.customer.CustomerResponse;
import com.codercollie.insurance_lab_core.exception.ResourceNotFoundException;
import com.codercollie.insurance_lab_core.mapper.CustomerMapper;
import com.codercollie.insurance_lab_core.persistence.entity.CustomerEntity;
import com.codercollie.insurance_lab_core.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomerService {

    private final CustomerMapper customerMapper;
    private final CustomerRepository customerRepository;

    public CustomerService(CustomerMapper customerMapper, CustomerRepository customerRepository) {
        this.customerMapper = customerMapper;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        final CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("customer not found"));

        return customerMapper.toResponse(customer);
    }

    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        final CustomerEntity customer = customerMapper.toEntity(request);
        final CustomerEntity savedCustomer = customerRepository.save(customer);
        return customerMapper.toResponse(savedCustomer);
    }

}
