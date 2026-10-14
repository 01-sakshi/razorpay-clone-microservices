package com.payment_service.processor;

import common_lib.dto.PaymentProcessorRequest;
import common_lib.dto.PaymentProcessorResponse;
import common_lib.enums.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentProcessorRouter {

    private final Map<PaymentMethod, PaymentProcessor> paymentProcessorMap;

    /** Dispatches a charge by payment method and throws when the method has no registered processor. */
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        PaymentProcessor paymentProcessor = paymentProcessorMap.get(request.paymentMethod());
        if(paymentProcessor == null) {
            throw new IllegalArgumentException("No payment router found for the payment method: " + request.paymentMethod());
        }
        return paymentProcessor.charge(request);
    }
}
