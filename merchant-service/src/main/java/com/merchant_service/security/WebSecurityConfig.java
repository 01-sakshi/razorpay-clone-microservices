package com.merchant_service.security;

import common_lib.idempotency.IdempotencyFilter;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final IdempotencyFilter idempotencyFilter;

//    If idempotency key is present, it will try to return the result for the request via redis cache,
//    else will pass the request to controller layer to fetch it from DB.
    @Bean
    public FilterRegistrationBean<Filter> idempotencyFilterRegistration() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>(idempotencyFilter);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 2);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
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
}
