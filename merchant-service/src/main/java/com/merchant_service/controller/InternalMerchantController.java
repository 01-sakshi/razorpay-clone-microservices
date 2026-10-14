package com.merchant_service.controller;

import com.merchant_service.api.MerchantLookupService;
import common_lib.dto.SettlementBankDetails;
import common_lib.dto.WebhookTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/merchants")
public class InternalMerchantController {

    private final MerchantLookupService merchantLookupService;

    @GetMapping("/active-merchant-ids")
    ResponseEntity<List<UUID>> getAllActiveMerchantIds() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(merchantLookupService.getAllActiveMerchants());
    }

    @GetMapping("{merchant-id}/settlement-bank-details")
    ResponseEntity<SettlementBankDetails> getSettlementBankDetails(@PathVariable UUID merchantId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(merchantLookupService.getSettlementBankDetails(merchantId));
    }

    @GetMapping("{merchant-id}/webhook-targets")
    ResponseEntity<List<WebhookTarget>> getActiveConfigsForEvent(@PathVariable UUID merchantId,
                                                                 @RequestParam String eventType) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(merchantLookupService.getActiveConfigsForEvent(merchantId, eventType));
    }
}
