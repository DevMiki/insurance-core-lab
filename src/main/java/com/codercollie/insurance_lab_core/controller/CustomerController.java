package com.codercollie.insurance_lab_core.controller;

import com.codercollie.insurance_lab_core.dto.customer.CreateCustomerRequest;
import com.codercollie.insurance_lab_core.dto.customer.CustomerResponse;
import com.codercollie.insurance_lab_core.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        return customerService.createCustomer(request);
    }

}
