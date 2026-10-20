package com.merchant_service.serviceImpl;

import com.merchant_service.dto.request.LoginRequest;
import com.merchant_service.dto.request.MerchantSignupRequest;
import com.merchant_service.dto.response.LoginResponse;
import com.merchant_service.dto.response.MerchantResponse;
import com.merchant_service.entity.AppUser;
import com.merchant_service.entity.Merchant;
import com.merchant_service.mapper.MerchantMapper;
import com.merchant_service.repository.AppUserRepository;
import com.merchant_service.repository.MerchantRepository;
import com.merchant_service.security.JwtUtil;
import com.merchant_service.service.AuthService;
import common_lib.constants.ErrorCode;
import common_lib.enums.UserRole;
import common_lib.exceptions.BusinessRuleViolationException;
import common_lib.exceptions.DuplicateResourceException;
import common_lib.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final MerchantRepository merchantRepository;
    private final AppUserRepository appUserRepository;
    private final MerchantMapper merchantMapper;
    private final PasswordEncoder passwordEncoder;  //Bean definition in WebSecurityConfig
    private final JwtUtil jwtUtil;

    /**
     * Rejects an existing merchant email, then transactionally saves the merchant and an OWNER account with a BCrypt password hash.
     *
     * @throws DuplicateResourceException if the email is already registered
     */
    @Override
    @Transactional
    public MerchantResponse signUp(MerchantSignupRequest merchantSignupRequest) {
        //Check if merchant already exists
        //We do not require pessimistic locking for this API, since any ways this API won't allow a
        //request to proceed with duplicate merchant email
        if (merchantRepository.findByEmail(merchantSignupRequest.email()) != null) {
            throw new DuplicateResourceException(ErrorCode.DUPLICATE_MERCHANT_EMAIL,
                    "Merchant already exists with email : " + merchantSignupRequest.email());
        }

        //prepare to persist merchant in database
        Merchant merchant = merchantMapper.toEntityFromSignUpRequest(merchantSignupRequest);
        merchant = merchantRepository.save(merchant);

        AppUser appUser = AppUser
                .builder()
                .merchant(merchant)
                .email(merchant.getEmail())
                .passwordHash(passwordEncoder.encode(merchantSignupRequest.password()))
                .role(UserRole.OWNER)
                .build();

        appUserRepository.save(appUser);
        return merchantMapper.toResponse(merchant);
    }

    /**
     * Authenticates the email/password pair, resolves its merchant and role, and issues a signed access token.
     */
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        AppUser appUser = appUserRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new ResourceNotFoundException("APP_USER", loginRequest.email()));

        if (!passwordEncoder.matches(loginRequest.password(), appUser.getPasswordHash())) {
            throw new BusinessRuleViolationException("INVALID_PASSWORD",
                    "Password is incorrect", "USER", loginRequest.password());
        }
        String jwtToken = jwtUtil.generateAccessToken(loginRequest.email(),
                appUser.getMerchant().getId(), appUser.getRole().name());
        return new LoginResponse(jwtToken);
    }
}
