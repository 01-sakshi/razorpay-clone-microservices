package com.operations_service.client;

import common_lib.dto.PaymentSettlementView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

/* name should match the service name of microservice */

//In case of k8s environment, since discovery service won't be there, payment service's URL will have to fetched from
//environment variable PAYMENT_SERVICE_URI. For dev environment, this variable's value will not be found, hence
// value/url will be fetched from name field.

@FeignClient(name = "payment-service", path = "/internal/payments", url = "${PAYMENT_SERVICE_URI:}")
public interface PaymentServiceClient {

    @GetMapping("/unsettled/captured")
    List<PaymentSettlementView> findUnsettledCaptured(@RequestParam UUID merchantId);

    @PostMapping("/mark-settled")
    void markSettled(@RequestBody List<UUID> paymentIds);

}
