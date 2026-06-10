package com.codercollie.insurance_lab_core.controller;

import com.codercollie.insurance_lab_core.dto.product.CreateProductRequest;
import com.codercollie.insurance_lab_core.dto.product.ProductResponse;
import com.codercollie.insurance_lab_core.exception.ResourceNotFoundException;
import com.codercollie.insurance_lab_core.service.ProductService;
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

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    void getsProductById() throws Exception {
        when(productService.getProductById(1L))
                .thenReturn(new ProductResponse(1L, "PROD-001", "toothbrush"));

        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.productCode", is("PROD-001")))
                .andExpect(jsonPath("$.name", is("toothbrush")));

        verify(productService).getProductById(1L);
    }

    @Test
    void returnsNotFoundWhenProductDoesNotExist() throws Exception {
        when(productService.getProductById(999L))
                .thenThrow(new ResourceNotFoundException("product not found"));

        mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("product not found")));

        verify(productService).getProductById(999L);
    }

    @Test
    void createsProduct() throws Exception {
        when(productService.createProduct(any(CreateProductRequest.class)))
                .thenReturn(new ProductResponse(1L, "PROD-001", "toothbrush"));

        final CreateProductRequest request = new CreateProductRequest("PROD-001", "toothbrush");

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.productCode", is("PROD-001")))
                .andExpect(jsonPath("$.name", is("toothbrush")));

        verify(productService).createProduct(
                new CreateProductRequest("PROD-001", "toothbrush")
        );
    }

    @Test
    void rejectsMissingProductCode() throws Exception {
        final CreateProductRequest request = new CreateProductRequest(null, "toothbrush");

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("productCode is required")));

        verify(productService, never()).createProduct(any(CreateProductRequest.class));
    }
}
