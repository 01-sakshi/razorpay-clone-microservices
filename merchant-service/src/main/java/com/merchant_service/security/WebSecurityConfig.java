package com.merchant_service.security;

import common_lib.idempotency.IdempotencyFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configures stateless JWT and API-key security chains with idempotency filtering.
 *
 * <p>Account and merchant-management routes use JWT authentication, payment and vault routes use API-key
 * authentication, and stored passwords are BCrypt-hashed. Credentials and password material are not returned or
 * logged by these beans.
 */
@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {

    private static final String[] JWT_ROUTES = {"/v1/actuator/**", "/v1/merchants/**", "/v1/admin/**", "/v1/auth/**"};
    private static final String[] API_KEY_ROUTES = {"/v1/orders/**", "/v1/payments/**", "/v1/vault/**"};
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    private final IdempotencyFilter idempotencyFilter;

    /**
     * Configures the stateless JWT chain for matched merchant/admin routes.
     *
     * @param httpSecurity builder for the JWT filter chain
     * @return the configured stateless JWT chain
     */
    @Bean
    @Order(1)
    public SecurityFilterChain jwtChain(HttpSecurity httpSecurity) {
        return httpSecurity
                .securityMatcher(JWT_ROUTES)      /* Only the routes that are allowed as per JWT_ROUTES will be permitted here and the following code will apply to them only */
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v1/auth/signup", "/v1/auth/login").permitAll()
                        .anyRequest().authenticated())
                .formLogin(formLogin -> formLogin.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) //Add jwtAuthenticationFilter before UsernamePasswordAuthenticationFilter in the security chain list
                .addFilterAfter(idempotencyFilter, JwtAuthenticationFilter.class)
                .build();
    }

    /**
     * Configures the stateless API-key chain for payment routes.
     *
     * @param httpSecurity builder for the API-key filter chain
     * @return the configured stateless API-key chain
     */
    @Bean
    @Order(2)
    public SecurityFilterChain apiKeyChain(HttpSecurity httpSecurity) {
        return httpSecurity
                .securityMatcher(API_KEY_ROUTES)      /* Only the routes that are allowed as per API_KEY_ROUTES will be permitted here and the following code will apply to them only */
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated())
                .formLogin(formLogin -> formLogin.disable())
                .addFilterBefore(apiKeyAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) //Add apiKeyAuthenticationFilter before UsernamePasswordAuthenticationFilter in the security chain list
                .addFilterAfter(idempotencyFilter, ApiKeyAuthenticationFilter.class)
                .build();
    }

    /**
     * Provides BCrypt encoding so persisted account passwords are hashes rather than plaintext.
     * From PasswordEncoder interface, specifically BCryptPasswordEncoder type bean will be created as per requirement
     *
     * @return password encoder for account authentication
     */
    @Bean
    public PasswordEncoder encodedPassword() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Wires email-based user lookup and the configured password encoder into the DAO authentication provider.
     *
     * @param merchantUserDetailsService email-based account lookup service
     * @param passwordEncoder            BCrypt password encoder
     * @return authentication manager backed by the DAO provider
     */
    @Bean
    public AuthenticationManager authenticationManager(MerchantUserDetailsService merchantUserDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(merchantUserDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(daoAuthenticationProvider);
    }
}
