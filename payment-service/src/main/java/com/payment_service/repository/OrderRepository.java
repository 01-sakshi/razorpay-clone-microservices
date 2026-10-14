package com.payment_service.repository;

import com.payment_service.entity.OrderRecord;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderRecord, UUID> {
    /** Tests receipt uniqueness within one merchant, permitting the same receipt value under a different merchant. */
    boolean existsByReceiptAndMerchant(String receipt, UUID merchantId);

    /** Tests both order ID and merchant ID without loading the order entity. */
    boolean existsByIdAndMerchant(UUID orderId, UUID merchantId);

    /** Loads an order only when its ID and merchant owner match; a foreign ID is returned as absent. */
    Optional<OrderRecord> findByIdAndMerchant(UUID orderId, UUID merchantId);

    /** Acquires a pessimistic write lock on the merchant-scoped order until the surrounding transaction completes. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrderRecord o where o.id = :orderId and o.merchant = :merchantId")
    Optional<OrderRecord> findByIdAndMerchantForUpdate(UUID orderId, UUID merchantId);
}
