package com.merchant_service.api;

import common_lib.dto.SettlementBankDetails;
import common_lib.dto.WebhookTarget;

import java.util.List;
import java.util.UUID;

public interface MerchantLookupService {
    /**
     * Finds this merchant's enabled webhook targets subscribed to the event; an {@code ALL} subscription matches it too.
     *
     * @param merchantId merchant whose configurations are searched
     * @param eventType  event name to match
     * @return matching delivery targets, including the secret needed to sign each delivery
     */
    List<WebhookTarget> getActiveConfigsForEvent(UUID merchantId, String eventType);

    /**
     * Returns IDs of merchants whose persisted status is {@code ACTIVE}, for platform-wide processing.
     */
    List<UUID> getAllActiveMerchants();

    /**
     * Loads the settlement destination registered to the merchant.
     *
     * @param merchantId merchant whose settlement account is requested
     * @return account number, IFSC, and account-holder name
     * @throws ResourceNotFoundException if the merchant does not exist
     */
    SettlementBankDetails getSettlementBankDetails(UUID merchantId);
}
