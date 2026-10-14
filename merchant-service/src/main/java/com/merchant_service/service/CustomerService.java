package com.merchant_service.service;

import java.util.UUID;

public interface CustomerService {

    /**
     * Finds a customer by merchant and email or persists a new profile; blank email denotes a guest and returns {@code null}.
     *
     * @param merchantId tenant that owns the customer record
     * @param name customer display name used when creating a record
     * @param phone contact number used when creating a record
     * @param email lookup key and contact email; blank values skip customer creation
     * @return existing or newly persisted customer ID, or {@code null} for a guest without email
     */
    UUID findOrCreate(UUID merchantId, String name, String phone, String email);
}
