package com.vault_service.repository;

import com.vault_service.entity.CardToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CardTokenRepository extends JpaRepository<CardToken, UUID> {
    /** Resolves the exact opaque token only when {@code revokedAt} is null; the query itself does not scope by merchant. */
    Optional<CardToken> findByTokenAndRevokedAtNull(String token);
}
