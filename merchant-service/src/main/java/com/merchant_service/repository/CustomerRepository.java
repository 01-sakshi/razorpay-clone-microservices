package com.merchant_service.repository;

import com.merchant_service.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    /** Finds a customer only within the merchant/email pair, allowing identical emails in different merchant accounts. */
    Optional<Customer> findByEmailAndMerchantId(String email, UUID merchantId);
}
