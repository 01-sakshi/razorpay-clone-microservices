package com.operations_service.settlement;

import com.operations_service.client.MerchantServiceClient;
import com.operations_service.client.PaymentServiceClient;
import common_lib.dto.PaymentSettlementView;
import common_lib.dto.SettlementBankDetails;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SettlementClientWrapper {

    private final MerchantServiceClient merchantServiceClient;
    private final PaymentServiceClient paymentServiceClient;

    @CircuitBreaker(name = "payment-service")
    @Retry(name = "payment-service")
    List<PaymentSettlementView> findUnsettledCaptured(@RequestParam UUID merchantId) {
        return paymentServiceClient.findUnsettledCaptured(merchantId);
    }

    @CircuitBreaker(name = "merchant-service")
    @Retry(name = "merchant-service")
    SettlementBankDetails getSettlementBankDetails(@PathVariable UUID merchantId) {
        return merchantServiceClient.getSettlementBankDetails(merchantId);
    }

    @CircuitBreaker(name = "payment-service")
    @Retry(name = "payment-service")
    void markSettled(@RequestBody List<UUID> paymentIds) {
        paymentServiceClient.markSettled(paymentIds);
    }
}
