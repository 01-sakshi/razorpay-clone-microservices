package com.payment_service.config;

import com.payment_service.gateway.PaymentAdapter;
import com.payment_service.gateway.adapter.CardPaymentAdapter;
import com.payment_service.gateway.adapter.NetBankingAdapter;
import com.payment_service.gateway.adapter.UpiPaymentAdapter;
import common_lib.enums.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentAdapterConfig {

    private final CardPaymentAdapter cardPaymentAdapter;
    private final NetBankingAdapter netBankingAdapter;
    private final UpiPaymentAdapter upiPaymentAdapter;

    /** Builds the immutable method-to-adapter registry used by the gateway router. */
    @Bean
    public Map<PaymentMethod, PaymentAdapter> paymentAdapterMap() {
        return Map.of(
                PaymentMethod.CARD, cardPaymentAdapter,
                PaymentMethod.UPI, upiPaymentAdapter,
                PaymentMethod.NETBANKING, netBankingAdapter
        );
    }
}
