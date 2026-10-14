package com.merchant_service.controller;

import com.merchant_service.dto.request.LoginRequest;
import com.merchant_service.dto.request.MerchantSignupRequest;
import com.merchant_service.dto.response.LoginResponse;
import com.merchant_service.dto.response.MerchantResponse;
import com.merchant_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public merchant registration and login endpoints.
 */
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a merchant through {@code POST /v1/auth/signup} and its initial owner account using the validated signup details; credentials and private
     * account fields are omitted from the public response.
     *
     * @param merchantSignupRequest validated merchant and owner registration details
     * @return HTTP 201 with the created merchant's public profile
     */
    @PostMapping("/signup")
    public ResponseEntity<MerchantResponse> signup(@RequestBody @Valid MerchantSignupRequest merchantSignupRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                authService.signUp(merchantSignupRequest)
        );
    }

    /**
     * Authenticates a merchant user through {@code POST /v1/auth/login} by email and password and issues a signed access token for subsequent requests.
     * Invalid credentials are reported by the authentication service as an authorization failure.
     *
     * @param loginRequest validated login credentials
     * @return HTTP 200 with the signed token and login response metadata
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(
                authService.login(loginRequest)
        );
    }
}
