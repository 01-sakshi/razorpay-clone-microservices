package com.merchant_service.controller;

import com.merchant_service.service.CustomerService;
import common_lib.dto.FindOrCreateCustomerRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internals/customers")
@RequiredArgsConstructor
public class InternalCustomerController {

    private final CustomerService customerService;

    @PostMapping("/find-or-create")
    ResponseEntity<UUID> findOrCreate(@RequestBody FindOrCreateCustomerRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(
                customerService.findOrCreate(
                        request.merchantId(),
                        request.customerName(),
                        request.phone(),
                        request.email()
                )
        );
    }
}
