package com.merchant_service.repository;

import com.merchant_service.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    /** Resolves the globally unique login account by email, including its stored password hash and role. */
    Optional<AppUser> findByEmail(String username);
}
