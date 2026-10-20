package com.merchant_service.controller;

import com.merchant_service.entity.ApiKey;
import com.merchant_service.repository.ApiKeyRepository;
import common_lib.cache.ApiKeyCacheEntry;
import common_lib.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/internals/api-keys")
public class InternalApiKeyController {

    private final ApiKeyRepository apiKeyRepository;

    @GetMapping("/{keyId}")
    public ResponseEntity<ApiKeyCacheEntry> findById(@PathVariable String keyId) {
        ApiKey apiKey = apiKeyRepository.findByKeyId(keyId)
            .orElseThrow(() -> new ResourceNotFoundException("apiKey", keyId));
        ApiKeyCacheEntry apiKeyEntry = new ApiKeyCacheEntry(apiKey.getMerchant().getId(),
                keyId,
                apiKey.getKeySecretHash(),
                apiKey.getPreviousKeySecretHash(),
                apiKey.getEnvironment(),
                apiKey.getEnabled(),
                apiKey.getGracePeriodExpiresAt());
        return ResponseEntity.status(HttpStatus.OK).body(apiKeyEntry);
    }
}
