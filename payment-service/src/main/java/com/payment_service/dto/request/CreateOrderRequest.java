package com.payment_service.dto.request;

import common_lib.entity.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Map;

/**
 * Request to create a merchant-owned order.
 *
 * @param notes     optional merchant metadata
 * @param expiresAt optional expiry; the service supplies its configured default when {@code null}
 * @param receipt   optional merchant-side order reference
 * @param amount    order amount and currency
 * @param customer  optional customer details used for customer lookup or creation
 */
public record CreateOrderRequest(
        Map<String, Object> notes,
        Instant expiresAt,
        String receipt,     //This is order-id at merchant's end
        @Valid
        @NotNull(message = "Amount is required")
        Money amount,

        @Valid //To make @Size and @Email annotations work put in CustomerDetails record
        CustomerDetails customer
) {

    /**
     * Optional customer details attached to an order.
     *
     * @param name  customer name, up to 50 characters, or {@code null}
     * @param phone customer phone, up to 20 characters, or {@code null}
     * @param email customer email, up to 30 characters and valid when present, or {@code null}
     */
    public record CustomerDetails(
            @Size(max = 50)
            String name,

            @Size(max = 20)
            String phone,

            @Email
            @Size(max = 30)
            String email
    ) {

    }
}
