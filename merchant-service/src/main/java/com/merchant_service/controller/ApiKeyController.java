package com.merchant_service.controller;

import com.merchant_service.dto.request.CreateApiKeyRequest;
import com.merchant_service.dto.response.ApiKeyCreateResponse;
import com.merchant_service.dto.response.ApiKeyResponse;
import com.merchant_service.service.ApiKeyService;
import common_lib.context.MerchantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Authenticated merchant endpoints for API-key lifecycle management.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/merchants/api-keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final MerchantContext merchantContext;

    /**
     * Issues an API key through {@code POST /v1/merchants/api-keys} for the authenticated merchant and requested environment. The raw secret is returned only in
     * this creation response; subsequent list responses expose key metadata, not secret material.
     *
     * @param createApiKeyRequest validated environment for the credentials
     * @return HTTP 201 with the key ID and one-time raw secret
     */
    @PostMapping
    public ResponseEntity<ApiKeyCreateResponse> create(@Valid @RequestBody CreateApiKeyRequest createApiKeyRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiKeyService.create(merchantContext.getMerchantId(), createApiKeyRequest));
    }

    /**
     * Lists API-key metadata through {@code GET /v1/merchants/api-keys} for the authenticated merchant without exposing stored secret hashes or raw secrets.
     *
     * @return HTTP 200 with the merchant's API keys
     */
    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> list() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(apiKeyService.list(merchantContext.getMerchantId()));
    }

    /**
     * Revokes a key through {@code DELETE /v1/merchants/api-keys/revoke/{keyId}} belonging to the authenticated merchant and invalidates its cached credentials.
     * A key owned by another merchant is treated as not found.
     *
     * @param keyId identifier of the key to revoke
     * @return HTTP 200 with the service's revocation result
     */
    @DeleteMapping("/revoke/{keyId}")
    public ResponseEntity<String> revoke(@PathVariable String keyId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(apiKeyService.revoke(merchantContext.getMerchantId(), keyId));
    }

    /**
     * Rotates an enabled key through {@code POST /v1/merchants/api-keys/rotate/{keyId}} belonging to the authenticated merchant, returning replacement credentials while the
     * service applies its configured grace period to the previous key.
     *
     * @param keyId identifier of the key to rotate
     * @return HTTP 201 with the replacement key ID and one-time raw secret
     */
    @PostMapping("/rotate/{keyId}")
    public ResponseEntity<ApiKeyCreateResponse> rotate(@PathVariable String keyId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(apiKeyService.rotate(merchantContext.getMerchantId(), keyId));
    }


}
