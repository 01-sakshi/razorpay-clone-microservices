package com.payment_service.strategy;

import com.payment_service.processor.PaymentProcessor;
import common_lib.dto.PaymentProcessorRequest;
import common_lib.dto.PaymentProcessorResponse;
import common_lib.util.RandomizerUtil;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class NetBankingPaymentProcessor implements PaymentProcessor {

    /** Returns {@code BANK_REJECTED} for bank code {@code BANK_CODE_FAIL}; other supplied bank codes receive a pending reference. */
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        final String BANK_CODE_FAIL = "BANK_CODE_FAIL";
        String bankCode = request.methodDetails() != null ? request.methodDetails().get("bankCode").toString() : null;

        //Simulation
        if (Objects.equals(bankCode, BANK_CODE_FAIL)) {
            return new PaymentProcessorResponse.Failure("BANK_REJECTED",
                    "Bank rejected the transaction");
        }

        String processorRef = "NET_BANKING_PROCESSOR_" + RandomizerUtil.randomBase64(16);
//        String redirectRef = "https://REDIRECT_BANK.com/" + processorRef;

        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
