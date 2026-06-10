package com.codercollie.insurance_lab_core.controller;

import com.codercollie.insurance_lab_core.dto.coverage.CoverageResponse;
import com.codercollie.insurance_lab_core.dto.coverage.CreateCoverageRequest;
import com.codercollie.insurance_lab_core.dto.product.CreateProductRequest;
import com.codercollie.insurance_lab_core.dto.product.ProductResponse;
import com.codercollie.insurance_lab_core.exception.ResourceNotFoundException;
import com.codercollie.insurance_lab_core.service.CoverageService;
import com.codercollie.insurance_lab_core.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @MockitoBean
    private CoverageService coverageService;

    @Test
    void addsCoverageToProduct() throws Exception {
        when(coverageService.addCoverageToProduct(eq(1L), any(CreateCoverageRequest.class)))
                .thenReturn(new CoverageResponse(
                        10L,
                        "FIRE",
                        "Fire coverage",
                        "Protects the insured home against fire damage",
                        new BigDecimal("120.00"),
                        1L
                ));

        final CreateCoverageRequest request = new CreateCoverageRequest(
                "FIRE",
                "Fire coverage",
                "Protects the insured home against fire damage",
                new BigDecimal("120.00")
        );

        mockMvc.perform(post("/api/v1/products/1/coverages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.code", is("FIRE")))
                .andExpect(jsonPath("$.name", is("Fire coverage")))
                .andExpect(jsonPath("$.description", is("Protects the insured home against fire damage")))
                .andExpect(jsonPath("$.basePrice", is(120.00)))
                .andExpect(jsonPath("$.productId", is(1)));

        verify(coverageService).addCoverageToProduct(
                1L,
                new CreateCoverageRequest(
                        "FIRE",
                        "Fire coverage",
                        "Protects the insured home against fire damage",
                        new BigDecimal("120.00")
                )
        );
    }

    @Test
    void listsCoveragesByProductId() throws Exception {
        when(coverageService.getCoveragesByProductId(1L))
                .thenReturn(List.of(new CoverageResponse(
                        10L,
                        "FIRE",
                        "Fire coverage",
                        "Protects the insured home against fire damage",
                        new BigDecimal("120.00"),
                        1L
                )));

        mockMvc.perform(get("/api/v1/products/1/coverages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(10)))
                .andExpect(jsonPath("$[0].code", is("FIRE")))
                .andExpect(jsonPath("$[0].productId", is(1)));

        verify(coverageService).getCoveragesByProductId(1L);
    }

    @Test
    void returnsNotFoundWhenListingCoveragesForMissingProduct() throws Exception {
        when(coverageService.getCoveragesByProductId(999L))
                .thenThrow(new ResourceNotFoundException("product not found"));

        mockMvc.perform(get("/api/v1/products/999/coverages"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("product not found")));

        verify(coverageService).getCoveragesByProductId(999L);
    }

    @Test
    void returnsNotFoundWhenAddingCoverageToMissingProduct() throws Exception {
        when(coverageService.addCoverageToProduct(eq(999L), any(CreateCoverageRequest.class)))
                .thenThrow(new ResourceNotFoundException("product not found"));

        final CreateCoverageRequest request = new CreateCoverageRequest(
                "FIRE",
                "Fire coverage",
                "Protects the insured home against fire damage",
                new BigDecimal("120.00")
        );

        mockMvc.perform(post("/api/v1/products/999/coverages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("product not found")));

        verify(coverageService).addCoverageToProduct(eq(999L), any(CreateCoverageRequest.class));
    }

    @Test
    void rejectsCoverageWithoutCode() throws Exception {
        final CreateCoverageRequest request = new CreateCoverageRequest(
                null,
                "Fire coverage",
                "Protects the insured home against fire damage",
                new BigDecimal("120.00")
        );

        mockMvc.perform(post("/api/v1/products/1/coverages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("code is required")));

        verify(coverageService, never()).addCoverageToProduct(any(Long.class), any(CreateCoverageRequest.class));
    }

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
