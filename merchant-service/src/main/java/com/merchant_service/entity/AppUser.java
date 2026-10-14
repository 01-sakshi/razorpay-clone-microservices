package com.merchant_service.entity;

import common_lib.entity.BaseAuditEntity;
import common_lib.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
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
public class AppUser extends BaseAuditEntity implements UserDetails {
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

    /** Converts the persisted role to the single Spring Security authority prefixed with {@code ROLE_}. */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
        );
    }

    /** Returns the encoded password hash used by the authentication provider; this is not the raw password. */
    @Override
    public @Nullable String getPassword() {
        return passwordHash;
    }

    /** Uses the account email as the Spring Security username. */
    @Override
    public String getUsername() {
        return email;
    }
}
