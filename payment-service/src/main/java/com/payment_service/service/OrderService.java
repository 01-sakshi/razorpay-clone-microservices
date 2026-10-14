package com.payment_service.service;

import com.payment_service.dto.request.CreateOrderRequest;
import com.payment_service.dto.response.OrderResponse;
import com.payment_service.dto.response.PaymentResponse;

import java.util.List;
import java.util.UUID;

/**
 * Provides merchant-scoped order lifecycle and payment-attempt lookup operations.
 */
public interface OrderService {
    /**
     * Creates a merchant-owned order, rejects duplicate receipts within that merchant, and returns the persisted representation.
     *
     * @param merchantId         owning merchant
     * @param createOrderRequest order amount and optional metadata, customer, receipt, and expiry
     * @return persisted order representation
     * @throws com.payment_gateway.razorpay.common.exceptions.DuplicateResourceException if the receipt is already used by the merchant
     */
    OrderResponse create(UUID merchantId, CreateOrderRequest createOrderRequest);

    /**
     * Retrieves by order and merchant IDs; absent and foreign orders are reported as not found.
     *
     * @param merchantId merchant scope
     * @param orderId    order identifier
     * @return order representation
     * @throws com.payment_gateway.razorpay.common.exceptions.ResourceNotFoundException if absent or foreign
     */
    OrderResponse get(UUID merchantId, UUID orderId);

    /**
     * Cancels a merchant-owned order unless it is already paid or cancelled, then returns a confirmation message.
     *
     * @param merchantId merchant scope
     * @param orderId    order identifier
     * @return cancellation confirmation
     * @throws com.payment_gateway.razorpay.common.exceptions.ResourceNotFoundException      if absent or foreign
     * @throws com.payment_gateway.razorpay.common.exceptions.BusinessRuleViolationException if already paid or cancelled
     */
    String cancel(UUID merchantId, UUID orderId);

    /**
     * Confirms merchant ownership before returning the order's payment attempts; unknown or foreign orders fail as not found.
     *
     * @param merchantId merchant scope
     * @param orderId    order identifier
     * @return payment-attempt representations, possibly empty
     * @throws com.payment_gateway.razorpay.common.exceptions.ResourceNotFoundException if the order is absent or foreign
     */
    List<PaymentResponse> list(UUID merchantId, UUID orderId);
}
