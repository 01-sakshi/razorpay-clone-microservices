package com.payment_service.strategy;

import com.payment_service.processor.PaymentProcessor;
import common_lib.dto.PaymentProcessorRequest;
import common_lib.dto.PaymentProcessorResponse;
import common_lib.util.RandomizerUtil;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UpiPaymentProcessor implements PaymentProcessor {
    /** Returns {@code UPI_REJECTED} for VPA {@code fail@okaxis}; other supplied VPAs receive a pending reference. */
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        final String VPA_CODE_FAIL = "fail@okaxis";
        String bankCode = request.methodDetails() != null ? request.methodDetails().get("vpa").toString() : null;

        //Simulation
        if (Objects.equals(bankCode, VPA_CODE_FAIL)) {
            return new PaymentProcessorResponse.Failure("UPI_REJECTED",
                    "Bank rejected the transaction");
        }

        String processorRef = "UPI_PROCESSOR_" + RandomizerUtil.randomBase64(16);
//        String bankRef = "BANK_REF" + RandomizerUtil.randomBase64(16);

        return new PaymentProcessorResponse.Pending(processorRef);
    }
}
