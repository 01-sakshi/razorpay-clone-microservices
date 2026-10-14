package com.merchant_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Merchant login credentials.
 *
 * @param email non-blank account email
 * @param password non-blank account password; never return or log it
 */
public record LoginRequest(
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password
) {
}
