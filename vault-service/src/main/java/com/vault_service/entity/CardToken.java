package com.vault_service.entity;

import common_lib.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "card_token"
)
@Getter
@Setter
@ToString 
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardToken extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String token;

    @Column(nullable = false)
    private UUID merchantId;

    private UUID customerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vault_card_id", nullable = false)
    @ToString.Exclude
    private VaultCard vaultCard;        //For one card, multiple card tokens can be generated for one or more merchants

    private Instant revokedAt;

}
