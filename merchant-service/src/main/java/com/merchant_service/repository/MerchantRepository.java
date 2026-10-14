package com.merchant_service.repository;

import com.merchant_service.entity.Merchant;
import common_lib.enums.MerchantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    /** Looks up the merchant whose globally unique signup email matches the supplied value. */
    Merchant findByEmail(String email);

    /** Selects only merchant IDs for the requested status, avoiding loading full merchant entities for batch work. */
    @Query("select merchant.id from Merchant merchant where merchant.status = :status")
    List<UUID> findAllIdsByStatus(@Param("status") MerchantStatus status);

}
