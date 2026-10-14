package com.payment_service.gateway.adapter;

import com.payment_service.client.VaultServiceClient;
import com.payment_service.gateway.PaymentAdapter;
import com.payment_service.gateway.dto.PaymentRequest;
import com.payment_service.gateway.dto.PaymentResult;
import common_lib.dto.PaymentProcessorResponse;
import common_lib.constants.ErrorCode;
import common_lib.dto.VaultChargeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardPaymentAdapter implements PaymentAdapter {

    private final VaultServiceClient vaultServiceClient;

    /**
     * Reads the card token from method details, asks the vault to decrypt and charge it, then maps the processor result.
     * Exceptions are converted to {@code CARD_PAYMENT_FAILED}; this adapter does not expose the PAN to its caller.
     *
     * @param paymentRequest payment and token details for the vault charge
     * @return pending, success, or failure gateway result
     */
    @Override
    public PaymentResult initiate(PaymentRequest paymentRequest) {
        log.info("Initiate Payment with CardPaymentAdapter, paymentId: {}", paymentRequest.paymentId());
        try {
            String token = (String) paymentRequest.methodDetails().get("token");
            PaymentProcessorResponse paymentProcessorResponse = vaultServiceClient.charge(
                    new VaultChargeRequest(paymentRequest.paymentId(), token,
                    paymentRequest.amount(), paymentRequest.methodDetails()));

            return switch (paymentProcessorResponse) {
                case PaymentProcessorResponse.Pending pending ->
                        new PaymentResult.Pending(pending.processorReference());
                case PaymentProcessorResponse.Failure failure ->
                        new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());
                case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());
            };
        } catch (Exception e) {
            log.error("CardPayment failed, paymentId: {}", paymentRequest.paymentId());
            return new PaymentResult.Failure(ErrorCode.CARD_PAYMENT_FAILED, e.getMessage());
        }
    }

    /**
     * Returns a fixed simulated success reference; no external card-network capture is performed here.
     *
     * @param paymentId payment identifier to capture
     * @return simulated successful capture result
     */
    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("CARD_REF");
    }
}
