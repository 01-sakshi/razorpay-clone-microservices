package com.payment_service.gateway;

import com.payment_service.gateway.dto.PaymentRequest;
import com.payment_service.gateway.dto.PaymentResult;

import java.util.UUID;

public interface PaymentAdapter {

    /** Submits the method-specific charge and normalizes the processor outcome as pending, success, or failure. */
    PaymentResult initiate(PaymentRequest paymentRequest);

    /** Requests capture for the payment ID using this adapter's method-specific gateway. */
    PaymentResult capture(UUID paymentId);
}
