package com.vault_service.controller;

import com.vault_service.dto.request.TokenizeRequest;
import com.vault_service.dto.response.TokenizeResponse;
import com.vault_service.service.VaultService;
import common_lib.context.MerchantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Authenticated merchant endpoint for PCI-sensitive card tokenization.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/vault")
public class VaultController {

    private final VaultService vaultService;
    private final MerchantContext merchantContext;

    /**
    * Validates card details through {@code POST /v1/vault/tokenize}, stores the card through the vault service under the authenticated merchant, and returns
     * token metadata rather than the stored card number.
     *
     * @param tokenizeRequest card data and expiry details to tokenize
     * @return HTTP 200 with the token and non-sensitive card metadata
     */
    @PostMapping("/tokenize")
    public ResponseEntity<TokenizeResponse> tokenize(@RequestBody @Valid TokenizeRequest tokenizeRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(
                vaultService.tokenize(tokenizeRequest, merchantContext.getMerchantId())
        );
    }
}
