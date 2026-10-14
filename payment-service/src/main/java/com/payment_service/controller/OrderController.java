package com.payment_service.controller;

import com.payment_service.dto.request.CreateOrderRequest;
import com.payment_service.dto.response.OrderResponse;
import com.payment_service.dto.response.PaymentResponse;
import com.payment_service.service.OrderService;
import common_lib.context.MerchantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Authenticated merchant endpoints for order creation, retrieval, cancellation, and payment-attempt lookup.
 */
@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MerchantContext merchantContext;

    /**
    * Creates an order through {@code POST /v1/orders} owned by the authenticated merchant. The service applies the configured default expiry when
     * the request omits one and publishes an order-created event after persistence.
     *
     * @param createOrderRequest order amount and optional receipt, customer, notes, and expiry
     * @return HTTP 201 with the persisted order representation
     */
    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest createOrderRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.create(merchantContext.getMerchantId(), createOrderRequest));
    }

    /**
    * Retrieves an order through {@code GET /v1/orders/{orderId}} only within the authenticated merchant's scope, so another merchant's order is not disclosed.
     *
     * @param orderId identifier of the order to retrieve
     * @return HTTP 200 with the order, or not found if it is absent or belongs to another merchant
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> get(@PathVariable UUID orderId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(orderService.get(merchantContext.getMerchantId(), orderId));
    }

    /**
    * Requests cancellation through {@code POST /v1/orders/cancel/{orderId}} of an order owned by the authenticated merchant. The service rejects paid or already
     * cancelled orders and publishes an order-cancelled event after a successful state change.
     *
     * @param orderId identifier of the order to cancel
     * @return HTTP 200 with the cancellation result
     */
    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<String> cancel(@PathVariable UUID orderId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(orderService.cancel(merchantContext.getMerchantId(), orderId));
    }

    /**
    * Lists payment attempts through {@code GET /v1/orders/{orderId}/payments} after verifying that the order belongs to the authenticated merchant.
     *
     * @param orderId identifier of the order whose payment attempts are requested
     * @return HTTP 200 with the order's payment attempts, or not found if it is absent or not merchant-owned
     */
    @GetMapping("/{orderId}/payments")
    public ResponseEntity<List<PaymentResponse>> list(@PathVariable UUID orderId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(orderService.list(merchantContext.getMerchantId(), orderId));
    }
}
