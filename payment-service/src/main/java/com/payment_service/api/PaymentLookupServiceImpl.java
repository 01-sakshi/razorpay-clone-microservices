package com.payment_service.api;

import com.payment_service.entity.Payment;
import com.payment_service.repository.PaymentRepository;
import common_lib.dto.PaymentSettlementView;
import common_lib.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentLookupServiceImpl implements PaymentLookupService {

    private final PaymentRepository paymentRepository;

    /**
     * Queries captured payments for the merchant using the repository's update lock for the enclosing transaction.
     */
    @Override
    public List<PaymentSettlementView> findUnsettledCapturedPayments(UUID merchantId) {
        List<Payment> paymentList = paymentRepository.findByMerchantIdAndStatusForUpdate(
                merchantId, PaymentStatus.CAPTURED);
        return paymentList.stream().map((payment -> {
            return new PaymentSettlementView(payment.getMerchantId(), payment.getAmount().getAmountUnits(),
                    payment.getAmount().getCurrency(), 0);  //TODO: Refund is yet to be handled
        })).toList();
    }

    @Override
    public void markSettled(List<UUID> paymentIds) {
        List<Payment> paymentList = paymentRepository.findAllById(paymentIds);
        paymentList.forEach(payment -> {
            payment.setStatus(PaymentStatus.SETTLED);
            payment.setSettledAt(Instant.now());
            paymentRepository.save(payment);
        });
    }

}
