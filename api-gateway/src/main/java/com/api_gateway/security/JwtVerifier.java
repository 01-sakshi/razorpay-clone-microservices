package com.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtVerifier {

    @Value("${jwt.secret}")
    private String secretKey;

    /**
     * Converts the configured UTF-8 JWT secret into the HMAC key used for signing and verification.
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    //Only for valid JWT Token, Claims is returned from here

    /**
     * Verifies the signature and parses a signed JWT; invalid signatures, malformed tokens, and expired tokens fail.
     *
     * @param accessToken compact bearer token
     * @return verified token claims
     */
    public Claims verifyAccessToken(String accessToken) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();
    }

    /**
     * Reads the {@code role} claim as a string from already verified claims.
     */
    public String extractRole(Claims claims) {
        return claims.get("role", String.class);
    }

    /**
     * Reads the {@code merchant_id} claim as a string from already verified claims.
     */
    public String extractMerchantId(Claims claims) {
        return claims.get("merchant_id", String.class);
    }
}
