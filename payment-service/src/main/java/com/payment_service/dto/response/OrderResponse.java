package com.payment_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import common_lib.entity.Money;
import common_lib.enums.OrderStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Public representation of a merchant-owned order and its payment-attempt state.
 *
 * @param orderId    order identifier
 * @param merchantId owning merchant identifier
 * @param customerId associated customer, or {@code null}
 * @param notes      merchant metadata, or {@code null}
 * @param receipt    merchant-side order reference, or {@code null}
 * @param amount     order amount and currency
 * @param status     current order status
 * @param attempts   number of payment attempts
 * @param expiresAt  order expiry time, or {@code null}
 * @param createdAt  creation time
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderResponse(
        UUID orderId,
        UUID merchantId,
        UUID customerId,
        Map<String, Object> notes,
        String receipt,     //This is order-id at merchant's end
        Money amount,
        OrderStatus status,
        Integer attempts,
        Instant expiresAt,
        Instant createdAt
) {
}
