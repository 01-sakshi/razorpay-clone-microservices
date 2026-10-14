package com.payment_service.api;

import common_lib.dto.PaymentSettlementView;

import java.util.List;
import java.util.UUID;

public interface PaymentLookupService {
    /** Finds captured payments for one merchant for settlement processing; results are locked by the repository query. */
    List<PaymentSettlementView> findUnsettledCapturedPayments(UUID merchantId);

    void markSettled(List<UUID> paymentIds);
}
