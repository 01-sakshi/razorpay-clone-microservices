package com.api_gateway.security;

import common_lib.exceptions.GatewayAuthenticationException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
//import common_lib.exceptions.GatewayAuthenticationException;

import java.util.Map;

@RequiredArgsConstructor
@Component
public class JwtAuthHandler {

    private final JwtVerifier jwtVerifier;

    public Map<String, String> authenticate(String jwtToken) {
        Claims claims = null;
        try {
            claims = jwtVerifier.verifyAccessToken(jwtToken);
        } catch (Exception e) {
            throw new GatewayAuthenticationException("JWT_AUTH_FAILED",
                    "USER", jwtToken, "JWT access token verification failed");
        }
        return Map.of("X-Merchant-Id", jwtVerifier.extractMerchantId(claims),
                "X-Merchant-Role", jwtVerifier.extractRole(claims));
    }
}

