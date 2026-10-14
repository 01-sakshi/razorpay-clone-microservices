package com.vault_service.entity;

import common_lib.entity.BaseAuditEntity;
import common_lib.enums.CardBrand;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "vault_card"
)
@Getter
@Setter
@ToString 
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VaultCard extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private byte[] encryptedPan;

    @Column(nullable = false)
    private byte[] encryptedDek;

    @Column(nullable = false, length = 6)
    private String bin;

    @Column(nullable = false, length = 4)
    private String lastFour;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CardBrand brand;

    @Column(nullable = false, length = 2)
    private String expiryMonth;

    @Column(nullable = false, length = 4)
    private String expiryYear;

    @Column(nullable = false)
    private String cardHolderName;

    private Instant deletedAt;
}
