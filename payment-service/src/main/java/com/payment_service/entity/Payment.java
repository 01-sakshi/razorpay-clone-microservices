package com.payment_service.entity;

import common_lib.entity.BaseAuditEntity;
import common_lib.entity.Money;
import common_lib.enums.PaymentMethod;
import common_lib.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "payment",
        indexes = {
                @Index(name = "idx_payment_id_order_id_merchant_id_status", columnList = "id, order_id, merchantId, status"),
                @Index(name = "idx_payment_id_merchant_id", columnList = "id, merchantId"),
                @Index(name = "idx_payment_merchant_id_status", columnList = "merchantId, status"),
        })
@Getter
@Setter
@ToString 
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    private OrderRecord orderRecord;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.CREATED;

    @Column(nullable = false)
    private UUID merchantId;

    @Embedded
    private Money amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> methodDetails;

    @Column(length = 30)
    private String idempotencyKey;  //Doubt: Why wasn't it added for OrderRecord?

    @Column(length = 100)
    private String bankReference;   //UTR details

    @Column(length = 100)
    private String processorReference;

    @Column(length = 20)
    private String errorCode;
    @Column(length = 255)
    private String errorDescription;

    private Instant authorizedAt;
    private Instant capturedAt;
    private Instant failedAt;
    private Instant refundedAt;
    private Instant settledAt;
}
