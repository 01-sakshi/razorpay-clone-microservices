package com.payment_service.config;

import com.payment_service.processor.PaymentProcessor;
import com.payment_service.strategy.CardPaymentProcessor;
import com.payment_service.strategy.NetBankingPaymentProcessor;
import com.payment_service.strategy.UpiPaymentProcessor;
import common_lib.enums.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentProcessorConfig {

    private final CardPaymentProcessor cardPaymentProcessor;
    private final UpiPaymentProcessor upiPaymentProcessor;
    private final NetBankingPaymentProcessor netBankingPaymentProcessor;

    /** Builds the immutable method-to-processor registry used by the processor router. */
    @Bean
    public Map<PaymentMethod, PaymentProcessor> paymentProcessorMap() {
        return Map.of(PaymentMethod.CARD, cardPaymentProcessor,
                PaymentMethod.UPI, upiPaymentProcessor,
                PaymentMethod.NETBANKING, netBankingPaymentProcessor);
    }
}
