package com.vault_service.processor;

import common_lib.dto.PaymentProcessorRequest;
import common_lib.dto.PaymentProcessorResponse;
import common_lib.util.RandomizerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CardPaymentProcessor {

    public static final String PAN_CARD_DECLINED = "4000000000000002";
    public static final String PAN_CARD_EXPIRED = "4000000000000002";

    /**
     * Returns a simulated decline for the configured decline PAN and a generated pending reference for other PANs.
     */
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        String pan = request.pan();
        if (PAN_CARD_DECLINED.equals(pan)) {
            log.error("PAN Card declined");
            return new PaymentProcessorResponse.Failure("PAN_CARD_DECLINED", "PAN Card declined");
        } else if (PAN_CARD_EXPIRED.equals(pan)) {
            log.error("PAN Card expired");
            return new PaymentProcessorResponse.Failure("PAN_CARD_EXPIRED", "PAN Card expired");
        }
        String processorReference = "CARD_PROCESSOR_" + RandomizerUtil.randomBase64(16);
        return new PaymentProcessorResponse.Pending(processorReference);
    }
}
