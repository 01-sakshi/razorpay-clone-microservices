package com.payment_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import common_lib.entity.Money;
import common_lib.enums.PaymentMethod;
import common_lib.enums.PaymentStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Public representation of a payment and its gateway outcome.
 *
 * @param id               payment identifier
 * @param merchantId       owning merchant identifier
 * @param orderId          related order identifier
 * @param status           current payment status
 * @param amount           payment amount and currency
 * @param method           payment method
 * @param methodDetails    method-specific response data, or {@code null}
 * @param bankReference    processor reference, or {@code null}
 * @param authorizedAt     authorization time, or {@code null}
 * @param capturedAt       capture time, or {@code null}
 * @param failedAt         failure time, or {@code null}
 * @param refundedAt       refund time, or {@code null}
 * @param settledAt        settlement time, or {@code null}
 * @param errorCode        gateway or domain error code, or {@code null}
 * @param errorDescription gateway or domain error description, or {@code null}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponse(
        UUID id,
        UUID merchantId,
        UUID orderId,
        PaymentStatus status,
        Money amount,
        PaymentMethod method,
        Map<String, Object> methodDetails,
        String bankReference,
        Instant authorizedAt,
        Instant capturedAt,
        Instant failedAt,
        Instant refundedAt,
        Instant settledAt,
        String errorCode,
        String errorDescription
) {
}
