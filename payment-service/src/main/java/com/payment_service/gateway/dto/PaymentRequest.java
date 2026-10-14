package com.payment_service.gateway.dto;

import common_lib.entity.Money;
import common_lib.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

/**
 * Gateway input for a payment authorization or initiation attempt.
 *
 * @param paymentId persisted payment identifier
 * @param merchantId owning merchant identifier
 * @param orderId related order identifier
 * @param paymentMethod selected gateway method
 * @param methodDetails method-specific input, potentially empty
 * @param amount amount to authorize
 */
public record PaymentRequest(
        UUID paymentId,
        UUID merchantId,
        UUID orderId,
        PaymentMethod paymentMethod,
        Map<String, Object> methodDetails,
        Money amount
) {
}
