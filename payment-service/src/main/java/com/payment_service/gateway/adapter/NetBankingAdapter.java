package com.payment_service.gateway.adapter;

import com.payment_service.gateway.PaymentAdapter;
import com.payment_service.gateway.dto.PaymentRequest;
import com.payment_service.gateway.dto.PaymentResult;
import com.payment_service.processor.PaymentProcessorRouter;
import common_lib.dto.PaymentProcessorRequest;
import common_lib.dto.PaymentProcessorResponse;
import common_lib.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NetBankingAdapter implements PaymentAdapter {

    private final PaymentProcessorRouter paymentProcessorRouter;

    /**
     * Creates a non-card processor request from the bank method details and maps pending, success, or failure back
     * to the gateway result; exceptions become {@code NETBANKING_FAILED} results.
     */
    @Override
    public PaymentResult initiate(PaymentRequest paymentRequest) {
        log.info("Initiate Payment with NetBankingAdapter, paymentId: {}", paymentRequest.paymentId());
        try {
            PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest.nonCard(paymentRequest.paymentId(),
                    paymentRequest.paymentMethod(), paymentRequest.amount(), paymentRequest.methodDetails()
            );

            //send request from PaymentAdapter to PaymentProcessor via PaymentProcessorRouter
            PaymentProcessorResponse paymentProcessorResponse = paymentProcessorRouter.charge(paymentProcessorRequest);

            return switch (paymentProcessorResponse) {
                case PaymentProcessorResponse.Pending pending ->
                        new PaymentResult.Pending(pending.processorReference());
                case PaymentProcessorResponse.Failure failure ->
                        new PaymentResult.Failure(failure.errorCode(), failure.errorDescription());
                case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());
            };
        } catch (Exception e) {
            log.error("Netbanking failed, paymentId: {}", paymentRequest.paymentId());
            return new PaymentResult.Failure(ErrorCode.NETBANKING_FAILED, e.getMessage());
        }
    }

    /** Returns a fixed simulated success reference; no external bank capture is performed here. */
    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("NET_BANKING_REF");
    }
}
