package common_lib.dto;

import common_lib.entity.Money;
import common_lib.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

/**
 * Internal processor request for card or non-card payment charging.
 *
 * @param processingId  unique processing request identifier
 * @param paymentId     persisted payment identifier
 * @param paymentMethod payment method
 * @param methodDetails method-specific details, or {@code null} when absent
 * @param pan           card PAN for card processing only; sensitive and never log or return it
 * @param expiry        card expiry for card processing only; {@code null} for non-card requests
 * @param amount        amount to charge
 */
public record PaymentProcessorRequest(
        UUID processingId,
        UUID paymentId,
        PaymentMethod paymentMethod,
        Map<String, Object> methodDetails,
        String pan,
        String expiry,      //Doubt: expiryMonth or expiryYear or both?
        Money amount
) {

    /**
     * Creates a card charge request with a fresh request ID and the PAN/expiry needed by the processor.
     *
     * @param paymentId     persisted payment identifier
     * @param pan           card PAN; sensitive and never log or return it
     * @param expiry        card expiry value
     * @param amount        amount to charge
     * @param methodDetails card-method details
     * @return a processor request containing the card fields
     */
    public static PaymentProcessorRequest card(UUID paymentId, String pan, String expiry, Money amount, Map<String, Object> methodDetails) {
        return new PaymentProcessorRequest(UUID.randomUUID(), paymentId, PaymentMethod.CARD, methodDetails, pan, expiry, amount);
    }

    /**
     * Creates a non-card charge request with a fresh request ID and null card fields.
     *
     * @param paymentId     persisted payment identifier
     * @param paymentMethod non-card payment method
     * @param amount        amount to charge
     * @param methodDetails method-specific details
     * @return a processor request with {@code null} PAN and expiry
     */
    public static PaymentProcessorRequest nonCard(UUID paymentId, PaymentMethod paymentMethod, Money amount, Map<String, Object> methodDetails) {
        return new PaymentProcessorRequest(UUID.randomUUID(), paymentId, paymentMethod, methodDetails, null, null, amount);
    }
}
