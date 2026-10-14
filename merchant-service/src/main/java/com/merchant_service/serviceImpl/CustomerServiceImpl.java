package com.merchant_service.serviceImpl;

import com.merchant_service.entity.Customer;
import com.merchant_service.entity.Merchant;
import com.merchant_service.repository.CustomerRepository;
import com.merchant_service.repository.MerchantRepository;
import com.merchant_service.service.CustomerService;
import common_lib.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final MerchantRepository merchantRepository;

        /** Reuses the customer with the same merchant/email pair; guests with blank email are deliberately left unlinked. */
    @Override
    public UUID findOrCreate(UUID merchantId, String name, String phone, String email) {
        /* if email in param is null, request could be from a guest account wherein user doesn't
        have an active account and only wants to place an order as a guest. */
        if (email == null || email.isBlank()) return null;

        return customerRepository.findByEmailAndMerchantId(email, merchantId)
                .map(Customer::getId)
                .orElseGet(() -> createNew(merchantId, name, phone, email));
    }

        /** Persists a new profile under the specified merchant and returns the generated database ID. */
    private UUID createNew(UUID merchantId, String name, String phone, String email) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("merchant", merchantId));
        Customer customer = Customer.builder()
                .merchant(merchant)
                .email(email)
                .name(name)
                .contactNumber(phone)
                .build();
        customer = customerRepository.save(customer);
        log.info("Customer created for merchant: {} with email: {}, name: {} and contact: {}",
                merchantId, email, name, phone);
        return customer.getId();
    }
}
