package com.operations_service.settlement;

import com.operations_service.settlement.dto.BankTransferResult;
import common_lib.dto.SettlementBankDetails;
import common_lib.entity.Money;

import java.util.UUID;

public interface BankTransferProcessor {

    /**
     * Submits the settlement's net amount to the merchant's settlement destination.
     *
     * @param settlementId persisted settlement being transferred
     * @param merchantId owner of the destination account
     * @param amount net amount to transfer
     * @param bankDetails merchant settlement account details
     * @return bank registration reference used to correlate the later transfer result
     */
    BankTransferResult initiate(UUID settlementId, UUID merchantId,
                                Money amount, SettlementBankDetails bankDetails);
}
