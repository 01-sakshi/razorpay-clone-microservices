package com.merchant_service.dto.response;

/**
 * Successful authentication response.
 *
 * @param accessToken signed bearer token for authenticated requests
 */
public record LoginResponse(
        String accessToken
) {
}
