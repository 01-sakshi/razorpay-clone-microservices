package com.payment_service.entity;

import common_lib.entity.BaseAuditEntity;
import common_lib.entity.Money;
import common_lib.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "order_record",
        indexes = {
                @Index(name = "idx_order_record_merchant_id_status", columnList = "merchant, status"),
                @Index(name = "idx_order_record_merchant_id_order_id", columnList = "merchant, id"),
        })
@Getter
@Setter
@ToString 
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRecord extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID merchant;      // TODO: Change column name to merchantId

    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.CREATED;

    @Column(length = 30)
    private String idempotencyKey;

    @Embedded
    private Money amount;

    @Column(nullable = true)
    private String receipt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> notes;

    /*payment attempts for the order*/
    @Builder.Default
    private int attempts = 0;

    private Instant expiresAt;
}
