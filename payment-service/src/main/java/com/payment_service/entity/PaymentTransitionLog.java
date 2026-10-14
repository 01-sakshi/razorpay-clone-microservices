package com.payment_service.entity;

import common_lib.entity.BaseAuditEntity;
import common_lib.enums.PaymentActor;
import common_lib.enums.PaymentEvent;
import common_lib.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "payment_transition_log",
        indexes = {
                @Index(name = "idx_payment_transition_payment_id", columnList = "payment_id")
        }
)
@Getter
@Setter
@ToString 
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentTransitionLog extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    @ToString.Exclude
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PaymentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PaymentStatus toStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentEvent event;

    /*could be system(razorpay itself) or admin(merchant) does it from admin dashboard*/
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private PaymentActor actor;

    private Instant occurredAt;
}