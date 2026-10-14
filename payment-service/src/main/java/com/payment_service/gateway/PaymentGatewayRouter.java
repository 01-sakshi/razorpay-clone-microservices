package com.payment_service.gateway;

import com.payment_service.gateway.dto.PaymentRequest;
import com.payment_service.gateway.dto.PaymentResult;
import common_lib.enums.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
/**
 * Selects the configured payment adapter for each payment method.
 */
public class PaymentGatewayRouter {

    /*Bean created via PaymentAdapterConfig*/
    private final Map<PaymentMethod, PaymentAdapter> paymentAdapterMap;

    /**
     * Dispatches initiation by payment method and fails fast when no adapter is registered.
     *
     * @param paymentRequest gateway initiation input
     * @return adapter result, including pending, success, or failure
     * @throws IllegalArgumentException if no adapter is registered for the request method
     */
    public PaymentResult initiate(PaymentRequest paymentRequest) {
        PaymentAdapter paymentAdapter = paymentAdapterMap.get(paymentRequest.paymentMethod());
        if (paymentAdapter == null)
            throw new IllegalArgumentException("No payment adapter found for the payment method: " + paymentRequest.paymentMethod());
        return paymentAdapter.initiate(paymentRequest);
    }

    /**
     * Dispatches capture by payment method and fails fast when no adapter is registered.
     *
     * @param paymentMethod payment method selecting the adapter
     * @param paymentId persisted payment identifier
     * @return adapter capture result
     * @throws IllegalArgumentException if no adapter is registered for the payment method
     */
    public PaymentResult capture(PaymentMethod paymentMethod, UUID paymentId) {
        PaymentAdapter paymentAdapter = paymentAdapterMap.get(paymentMethod);
        if (paymentAdapter == null)
            throw new IllegalArgumentException("No payment adapter found for the payment method: " + paymentMethod);
        return paymentAdapter.capture(paymentId);
    }
}
