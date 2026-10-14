package common_lib.dto;

import java.util.UUID;


public record PaymentSettlementView(
        UUID paymentId,
        long amountUnits,
        String currency,
        int refundAmountUnits
) {
}
