package com.payment_service.service;

import com.payment_service.dto.request.PaymentInitRequest;
import com.payment_service.dto.response.PaymentResponse;

import java.util.UUID;

/**
 * Coordinates merchant-scoped payment attempts, captures, and asynchronous authorization outcomes.
 */
public interface PaymentService {
    /**
     * Starts a payment attempt for a merchant-owned payable order.
     *
     * <p>The implementation serializes attempts for the order, persists the attempt, routes authorization, and
     * publishes the resulting payment event. A pending gateway result returns the persisted attempt; a gateway
     * failure returns the failed payment state, while the current immediate-success branch returns {@code null}.
     *
     * @param merchantId merchant that owns the order and payment attempt
     * @param request    order identifier, payment method, and method-specific details
     * @return the persisted payment representation, or {@code null} for the current immediate-success branch
     * @throws com.payment_gateway.razorpay.common.exceptions.ResourceNotFoundException      if the order is absent or not owned by the merchant
     * @throws com.payment_gateway.razorpay.common.exceptions.BusinessRuleViolationException if the order is not payable
     */
    PaymentResponse initiate(UUID merchantId, PaymentInitRequest request);

    /**
     * Captures a merchant-owned payment through the configured gateway.
     *
     * @param merchantId merchant that owns the payment
     * @param paymentId  identifier of the payment to capture
     * @return the persisted payment representation after the capture result
     * @throws com.payment_gateway.razorpay.common.exceptions.ResourceNotFoundException       if the payment is absent or not owned by the merchant
     * @throws com.payment_gateway.razorpay.common.exceptions.InvalidStateTransitionException if the payment cannot enter capture
     */
    PaymentResponse capture(UUID merchantId, UUID paymentId);

    /**
     * Applies an asynchronous authorization decision and auto-captures an approved payment.
     *
     * @param paymentId        identifier of the payment whose authorization result was received
     * @param approve          whether authorization succeeded
     * @param bankRef          bank reference supplied by the callback; the current implementation does not consume it
     * @param errorCode        gateway error code supplied for a declined result
     * @param errorDescription gateway error description supplied for a declined result
     * @throws com.payment_gateway.razorpay.common.exceptions.ResourceNotFoundException if the payment is absent
     */
    void resolveAuthorization(UUID paymentId, boolean approve, String bankRef, String errorCode, String errorDescription);
}
