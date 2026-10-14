package com.merchant_service.security;

import com.merchant_service.repository.AppUserRepository;
import common_lib.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MerchantUserDetailsService implements UserDetailsService {
    /* For spring security JWT Authentication, this MerchantUserDetailsService implements UserDetailsService */

    private final AppUserRepository appUserRepository;

    /**
     * Resolves an application account by email for Spring Security authentication.
     *
     * @param username account email supplied by the authentication provider
     * @return the persisted user details, including encoded credentials and authorities
     * @throws ResourceNotFoundException if no account has that email
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return appUserRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("APPUSER", username));
    }
}
