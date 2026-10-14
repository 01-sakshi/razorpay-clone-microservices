package com.merchant_service.test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

/**
 * Test-only webhook receiver used to exercise delivery callbacks.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/merchant/webhooks")
public class DummyRealMerchantController {
    /* TEST API */
    /** Accepts {@code POST /v1/merchant/webhooks}, ignores the supplied test payload, and returns the fixed HTTP 200 body used by the dummy webhook endpoint.
     *
     * @param o arbitrary test request body
     * @return HTTP 200 with the fixed test-success body
     */
    @PostMapping
    public ResponseEntity<String> test(@RequestBody Object o) {
        return ResponseEntity.ok("Test Successfull");
    }
}
