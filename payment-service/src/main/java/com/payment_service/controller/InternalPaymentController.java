package com.payment_service.controller;

import com.payment_service.api.PaymentLookupService;
import common_lib.dto.PaymentSettlementView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/payments")
public class InternalPaymentController {

    private final PaymentLookupService paymentLookupService;

    @GetMapping("/unsettled/captured")
    ResponseEntity<List<PaymentSettlementView>> findUnsettledCaptured(@RequestParam UUID merchantId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(paymentLookupService.findUnsettledCapturedPayments(merchantId));
    }

    @PostMapping("/mark-settled")
    ResponseEntity<Void> markSettled(@RequestBody List<UUID> paymentIds) {
        paymentLookupService.markSettled(paymentIds);
        return ResponseEntity.noContent().build();
    }
}
