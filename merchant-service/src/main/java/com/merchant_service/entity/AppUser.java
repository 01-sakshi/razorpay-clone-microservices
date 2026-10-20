package com.merchant_service.entity;

import common_lib.entity.BaseAuditEntity;
import common_lib.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "app_user",
        indexes = {
                @Index(name = "idx_app_user_merchant_id", columnList = "merchant_id")
        })
@Getter
@Setter
@ToString 
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppUser extends BaseAuditEntity {
    /* For spring security JWT Authentication, this AppUser implements UserDetails */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id")
//    @ToString.Exclude
    private Merchant merchant;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
}
