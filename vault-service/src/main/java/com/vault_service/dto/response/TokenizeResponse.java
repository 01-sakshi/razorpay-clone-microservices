package com.vault_service.dto.response;


import common_lib.enums.CardBrand;

/**
 * Non-sensitive result of card tokenization.
 *
 * @param token vault token used for later payment processing
 * @param expiryMonth stored expiry month
 * @param expiryYear stored expiry year
 * @param cardBrand detected card brand
 * @param lastFour final four digits for display; the full PAN is never returned
 */
public record TokenizeResponse(
        String token,
        Integer expiryMonth,
        Integer expiryYear,
        CardBrand cardBrand,
        String lastFour
) {
}
