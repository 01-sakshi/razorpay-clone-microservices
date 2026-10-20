package com.api_gateway.security;

import common_lib.exceptions.GatewayAuthenticationException;
import common_lib.exceptions.RateLimitException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
public class GatewayAuthFilter extends OncePerRequestFilter {

    private final String BEARER_PREFIX = "Bearer ";
    private final String BASIC_PREFIX = "Basic ";
    private final ApiKeyAuthHandler apiKeyAuthHandler;
    private final JwtAuthHandler jwtAuthHandler;
    private final PublicRouteMatcher publicRouteMatcher;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.info("Incoming request : {}", request.getRequestURI());
        if (publicRouteMatcher.isPublic(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Map<String, String> headers;
            String authorization = request.getHeader("Authorization");
            if (authorization.startsWith(BEARER_PREFIX)) {
                headers = jwtAuthHandler.authenticate(authorization.substring(BEARER_PREFIX.length()));
            } else if (authorization.startsWith(BASIC_PREFIX)) {
                headers = apiKeyAuthHandler.authenticate(authorization, response);
            } else {
                reject(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing or invalid Authorization header");
                return;
            }

            HeaderAugmentingRequestWrapper requestWrapper = new HeaderAugmentingRequestWrapper(request);
            headers.forEach((name, value) -> requestWrapper.putHeader(name, value));
            filterChain.doFilter(requestWrapper, response);
        } catch (RateLimitException e) {
            response.setHeader("Retry-After", String.valueOf(e.getRetryAfterSeconds()));
            reject(response, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", e.getMessage());
        } catch (GatewayAuthenticationException e) {
            reject(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", e.getMessage());
        } catch (Exception e) {
            log.warn("Gateway auth failed for path={}", request.getRequestURI(), e);
            reject(response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid credentials");
        }
    }

    private void reject(HttpServletResponse response, HttpStatus status, String errorCode, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), Map.of("errorCode", errorCode, "errorDescription", message));
    }
}
