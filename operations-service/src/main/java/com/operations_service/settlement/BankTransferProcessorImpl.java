package com.operations_service.settlement;

import com.operations_service.settlement.dto.BankTransferResult;
import common_lib.dto.SettlementBankDetails;
import common_lib.entity.Money;
import common_lib.util.RandomizerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BankTransferProcessorImpl implements BankTransferProcessor {
    /**
     * Generates a synthetic transfer registration reference; this implementation does not call a bank API.
     */
    @Override
    public BankTransferResult initiate(UUID settlementId, UUID merchantId, Money amount,
                                       SettlementBankDetails bankDetails) {

        // Call the Payment Gateway's Bank API
        String registrationRef = "TXN_" + RandomizerUtil.randomBase64(12);
        log.debug("Bank Transfer call completed for settlementId: {}, registrationRef: {}",
                settlementId, registrationRef);
        return new BankTransferResult(registrationRef);
    }
}
