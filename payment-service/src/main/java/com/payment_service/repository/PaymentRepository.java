package com.payment_service.repository;

import com.payment_service.entity.Payment;
import common_lib.enums.PaymentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    /** Lists attempts by order ID; callers must verify order ownership before exposing the results. */
    List<Payment> findAllByOrderRecordId(UUID orderId);

    /** Loads a payment only when both payment ID and merchant ID match; a foreign payment is absent. */
    Optional<Payment> findByIdAndMerchantId(UUID paymentId, UUID merchantId);

    /** Acquires a pessimistic write lock on a payment within the merchant scope until transaction completion. */
    @Query("select p from Payment p where p.id = :paymentId and p.merchantId = :merchantId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Payment> findByIdAndMerchantIdForUpdate(UUID paymentId, UUID merchantId);

    /** Selects payments globally by status and update cutoff for the authorization callback simulator. */
    List<Payment> findByStatusAndUpdatedAtBefore(PaymentStatus paymentStatus, Instant globalWindow);

    /** Acquires a pessimistic write lock by payment ID for an internal callback that has no merchant context. */
    @Query("select p from Payment p where p.id = :paymentId")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Payment> findByIdForUpdate(UUID paymentId);

    /*
     * @Lock(LockModeType.PESSIMISTIC_WRITE) adds a FOR UPDATE clause at the end of
     * select query.
     * It prevents another transaction from modifying or locking those same rows
     * until your transaction commits/rolls back.
     * e.g : select p from Payment p where p.merchantId = :merchantId and p.status =
     * :status and p.settledAt is null FOR UPDATE;
     */
    /** Locks rows matching merchant and status whose {@code settledAt} is null, preventing concurrent settlement selection. */
    @Query("select p from Payment p where p.merchantId = :merchantId and p.status = :status and p.settledAt is null")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Payment> findByMerchantIdAndStatusForUpdate(UUID merchantId, PaymentStatus status);

    Optional<Payment> findByMerchantIdAndIdempotencyKey(UUID merchantId, String idempotencyKey);
}
