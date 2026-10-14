package com.operations_service.entity;

import common_lib.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(
        name = "settlement_payment"
)
@Getter
@Setter
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
@ToString 
public class SettlementPayment extends BaseAuditEntity {

    @EmbeddedId
    private SettlementPaymentId settlementPaymentId;

    @MapsId("settlementId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "settlement_id", nullable = false)
    @ToString.Exclude
    private Settlement settlement;
}
