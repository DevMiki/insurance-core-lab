package com.codercollie.insurance_lab_core.controller;

import com.codercollie.insurance_lab_core.dto.payment.CreatePaymentRequest;
import com.codercollie.insurance_lab_core.dto.payment.PaymentResponse;
import com.codercollie.insurance_lab_core.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/policies/{policyId}/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PaymentResponse> getPaymentsForPolicy(@PathVariable Long policyId) {
        return paymentService.getPaymentsForPolicy(policyId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse createPayment(
            @PathVariable Long policyId,
            @Valid @RequestBody CreatePaymentRequest paymentRequest
    ) {
        return paymentService.createPayment(policyId, paymentRequest);
    }
}
