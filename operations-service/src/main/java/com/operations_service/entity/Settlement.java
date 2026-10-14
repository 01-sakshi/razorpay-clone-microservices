package com.operations_service.entity;

import common_lib.entity.BaseAuditEntity;
import common_lib.entity.Money;
import common_lib.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "settlement"
)
@Getter
@Setter
@ToString 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
public class Settlement extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID merchantId;

    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "gross_amount_units", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "gross_currency", nullable = false))
    })
    private Money grossAmount;

    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "refund_amount_units", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "refund_currency", nullable = false))
    })
    private Money refundAmount;

    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "fee_amount_units", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "fee_currency", nullable = false))
    })
    private Money feeAmount;

    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "gst_amount_units", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "gst_currency", nullable = false))
    })
    private Money gstAmount;

    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "net_amount_units", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "net_currency", nullable = false))
    })
    private Money netAmount;

    @Enumerated(EnumType.STRING)
    private SettlementStatus status;

    private String bankReference;

    private Instant settledAt;  //processedAt

    private String failureReason;
}
