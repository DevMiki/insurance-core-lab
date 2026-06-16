package com.codercollie.insurance_lab_core.controller;

import com.codercollie.insurance_lab_core.dto.customer.CreateCustomerRequest;
import com.codercollie.insurance_lab_core.dto.customer.CustomerResponse;
import com.codercollie.insurance_lab_core.exception.ResourceNotFoundException;
import com.codercollie.insurance_lab_core.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @Test
    void returnsCustomerWhenCustomerExists() throws Exception {
        when(customerService.getCustomerById(1L))
                .thenReturn(new CustomerResponse(1L, "CUS-001", "Mario Rossi"));

        mockMvc.perform(get("/api/v1/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.customerCode", is("CUS-001")))
                .andExpect(jsonPath("$.fullName", is("Mario Rossi")));

        verify(customerService).getCustomerById(1L);
    }

    @Test
    void returnsNotFoundWhenCustomerDoesNotExist() throws Exception {
        when(customerService.getCustomerById(999L))
                .thenThrow(new ResourceNotFoundException("customer not found"));

        mockMvc.perform(get("/api/v1/customers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("customer not found")));

        verify(customerService).getCustomerById(999L);
    }

    @Test
    void returnsCreatedCustomerWhenRequestIsValid() throws Exception {
        when(customerService.createCustomer(any(CreateCustomerRequest.class)))
                .thenReturn(new CustomerResponse(1L, "CUS-001", "Mario Rossi"));

        CreateCustomerRequest request = new CreateCustomerRequest("CUS-001", "Mario Rossi");

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.customerCode", is("CUS-001")))
                .andExpect(jsonPath("$.fullName", is("Mario Rossi")));

        verify(customerService).createCustomer(
                new CreateCustomerRequest("CUS-001", "Mario Rossi")
        );
    }

    @Test
    void rejectsMissingCustomerCode() throws Exception {
        final CreateCustomerRequest request = new CreateCustomerRequest(null, "Mario Rossi");

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("customerCode is required")));

        verify(customerService, never()).createCustomer(any(CreateCustomerRequest.class));
    }
}
