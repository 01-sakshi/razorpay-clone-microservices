package com.merchant_service.entity;

import common_lib.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "customer",
        indexes = {
                @Index(name = "idx_customer_merchant_id", columnList = "merchant_id")
        })
@Getter
@Setter
@ToString 
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Customer extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    @ToString.Exclude
    private Merchant merchant;      //Doubt

    private String name;

    @Column(length = 50)
    private String email;

    @Column(length = 20)
    private String contactNumber;

    @Column(length = 20)
    private String gstId;
}
