package com.payment_service.dto.request;

import common_lib.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * Request to authorize a payment attempt for an existing order.
 *
 * @param orderId order being attempted
 * @param paymentMethod selected payment method
 * @param methodDetails method-specific data; may be empty or {@code null} according to the selected gateway
 */
public record PaymentInitRequest(
        @NotNull(message = "Order Id is required")
        UUID orderId,
        PaymentMethod paymentMethod,
        Map<String, Object> methodDetails
) {
}
