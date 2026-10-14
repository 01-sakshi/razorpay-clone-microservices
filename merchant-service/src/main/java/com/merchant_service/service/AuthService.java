package com.merchant_service.service;

import com.merchant_service.dto.request.LoginRequest;
import com.merchant_service.dto.request.MerchantSignupRequest;
import com.merchant_service.dto.response.LoginResponse;
import com.merchant_service.dto.response.MerchantResponse;

public interface AuthService {
    /** Persists a merchant and owner account after rejecting an already-registered email, then returns the public profile. */
    MerchantResponse signUp(MerchantSignupRequest merchantSignupRequest);

    /** Authenticates the supplied credentials and returns a signed access token carrying the account's merchant and role claims. */
    LoginResponse login(LoginRequest loginRequest);
}
