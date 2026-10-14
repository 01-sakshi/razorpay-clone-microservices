package com.payment_service.controller;

import com.payment_service.dto.request.PaymentInitRequest;
import com.payment_service.dto.response.PaymentResponse;
import com.payment_service.service.PaymentService;
import common_lib.context.MerchantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Authenticated merchant endpoints for initiating and capturing payments.
 */
@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final MerchantContext merchantContext;

    /**
    * Starts a payment attempt through {@code POST /v1/payments} for an order in the authenticated merchant's scope. The payment service checks the
     * order's payable state, routes the attempt to its configured processor, and publishes the resulting event.
     *
     * @param paymentInitRequest order ID, payment method, and method-specific details
     * @return HTTP 201 with the initial payment-attempt state
     */
    @PostMapping()
    public ResponseEntity<PaymentResponse> initiate(@Valid @RequestBody PaymentInitRequest paymentInitRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.initiate(merchantContext.getMerchantId(), paymentInitRequest));
    }

    /**
    * Requests capture through {@code POST /v1/payments/capture/{paymentId}} of a payment owned by the authenticated merchant; the service applies the capture result to
     * the payment state and publishes the status change.
     *
     * @param paymentId identifier of the payment to capture
     * @return HTTP 200 with the updated payment state
     */
    @PostMapping("/capture/{paymentId}")
    public ResponseEntity<PaymentResponse> capture(@PathVariable UUID paymentId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(paymentService.capture(merchantContext.getMerchantId(), paymentId));
    }
}
