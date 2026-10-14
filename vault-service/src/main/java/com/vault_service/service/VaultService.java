package com.vault_service.service;

import com.vault_service.dto.request.TokenizeRequest;
import com.vault_service.dto.response.TokenizeResponse;
import common_lib.entity.Money;
import common_lib.dto.PaymentProcessorResponse;

import java.util.Map;
import java.util.UUID;

public interface VaultService {
    /**
     * Encrypts and persists card data under the merchant, returning an opaque token and non-sensitive card summary.
     *
     * @param tokenizeRequest validated card and optional customer details
     * @param merchantId merchant that owns the resulting token
     * @return token, expiry, detected brand, and last four digits; never the PAN
     */
    TokenizeResponse tokenize(TokenizeRequest tokenizeRequest, UUID merchantId);

    /**
     * Resolves a non-revoked token, decrypts its card data for the processor, and returns the processor outcome;
     * decryption, lookup, and routing failures are returned as a failure result.
     *
     * @param paymentId payment attempt associated with the charge
     * @param token opaque vault token
     * @param amount amount to charge
     * @param methodDetails additional card method data forwarded to the processor
     * @return pending, success, or failure processor result
     */
    PaymentProcessorResponse charge(UUID paymentId, String token, Money amount, Map<String, Object> methodDetails);
}
