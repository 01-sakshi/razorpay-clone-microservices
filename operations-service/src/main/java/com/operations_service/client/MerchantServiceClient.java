package com.operations_service.client;

import common_lib.dto.SettlementBankDetails;
import common_lib.dto.WebhookTarget;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

/* name should match the service name of microservice */
@FeignClient(name = "merchant-service", path = "/internal/merchants")
public interface MerchantServiceClient {

    /* For all the places in operations service in the corresponding monolith application, wherever
     * merchantLookupService was utilized, we will create a corresponding method here in merchantServiceClient. */

    @GetMapping("/active-merchant-ids")
    List<UUID> getAllActiveMerchantIds();

    @GetMapping("{merchant-id}/settlement-bank-details")
    SettlementBankDetails getSettlementBankDetails(@PathVariable UUID merchantId);

    @GetMapping("{merchant-id}/webhook-targets")
    List<WebhookTarget> getActiveConfigsForEvent(@PathVariable UUID merchantId, @RequestParam String eventType);

}
