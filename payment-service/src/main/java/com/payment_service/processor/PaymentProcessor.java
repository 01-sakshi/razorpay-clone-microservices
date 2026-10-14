package com.payment_service.processor;

import common_lib.dto.PaymentProcessorRequest;
import common_lib.dto.PaymentProcessorResponse;

public interface PaymentProcessor {
    /** Submits a normalized method-specific charge request and reports whether it is pending, successful, or failed. */
    PaymentProcessorResponse charge(PaymentProcessorRequest request);
}
