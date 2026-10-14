package com.vault_service.controller;

import com.vault_service.service.VaultService;
import common_lib.dto.PaymentProcessorResponse;
import common_lib.dto.VaultChargeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/vaults")
public class InternalVaultController {

    private final VaultService vaultService;

    @PostMapping("/charge")
    ResponseEntity<PaymentProcessorResponse> charge(@RequestBody VaultChargeRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(
                vaultService.charge(request.paymentId(), request.token(),
                        request.amount(), request.methodDetails())
        );
    }

}
