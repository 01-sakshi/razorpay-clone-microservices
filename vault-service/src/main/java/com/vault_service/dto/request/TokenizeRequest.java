package com.vault_service.dto.request;

import com.vault_service.serviceImpl.validation.ExpiryYear;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.LuhnCheck;

import java.util.UUID;

/**
 * Validated card data submitted for merchant-scoped tokenization.
 *
 * @param customerId customer that owns the card token
 * @param pan 13- to 19-digit Luhn-valid card number; sensitive and never log or return it
 * @param expiryMonth expiry month from 1 through 12
 * @param expiryYear non-expired card expiry year
 * @param cvv three- or four-digit security code; sensitive and never log or persist in responses
 * @param cardHolderName optional cardholder name of at least three characters when present
 */
public record TokenizeRequest(
        @NotNull
        UUID customerId,

        @NotBlank(message = "PAN is required")
        @LuhnCheck(message = "Invalid PAN")     //TODO: Study Luhn Algorithm
        @Pattern(regexp = "^[0-9]{13,19}$", message = "PAN length is invalid")
        String pan,

        @NotNull(message = "Expiry month is required")
        @Min(value = 1, message = "Expiry month must be between 1 and 12")
        @Max(value = 12, message = "Expiry month must be between 1 and 12")
        Integer expiryMonth,

        @NotNull(message = "Expiry year is required")
        @ExpiryYear
        Integer expiryYear,

        @NotBlank
        @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV is invalid")
        String cvv,             //Doubt: Why are we sharing cvv with vault service?

        @Size(min = 3, message = "Card Holder Name should be minimum 3 characters")
        String cardHolderName
) {
}
