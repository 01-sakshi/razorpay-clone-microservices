package com.vault_service.serviceImpl;

import com.vault_service.config.VaultServiceConfig;
import com.vault_service.dto.request.TokenizeRequest;
import com.vault_service.dto.response.TokenizeResponse;
import com.vault_service.entity.CardToken;
import com.vault_service.entity.VaultCard;
import com.vault_service.processor.CardPaymentProcessor;
import com.vault_service.repository.CardTokenRepository;
import com.vault_service.repository.VaultCardRepository;
import com.vault_service.service.VaultService;
import common_lib.entity.Money;
import common_lib.enums.CardBrand;
import common_lib.exceptions.ResourceNotFoundException;
import common_lib.util.RandomizerUtil;
import common_lib.dto.PaymentProcessorResponse;
import common_lib.dto.PaymentProcessorRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaultServiceImpl implements VaultService {

    private final CardTokenRepository cardTokenRepository;
    private final VaultCardRepository vaultCardRepository;
    private final BytesEncryptor decEncryptor;
    private final CardPaymentProcessor cardPaymentProcessor;

    /**
     * Generates a per-card data key, encrypts the PAN and then encrypts that key with the vault encryptor, persists
     * both vault rows transactionally, and returns an opaque merchant/customer token with only card summary fields.
     *
     * @param tokenizeRequest card data and optional customer association
     * @param merchantId      owner assigned to the token
     * @return token plus expiry, detected brand, and last four digits
     */
    @Override
    @Transactional
    public TokenizeResponse tokenize(TokenizeRequest tokenizeRequest, UUID merchantId) {
        String lastFour = tokenizeRequest.pan().substring(tokenizeRequest.pan().length() - 4);
        String bin = tokenizeRequest.pan().substring(0, 6); // First six digits of PAN
        CardBrand cardBrand = detectBrand(bin);
        byte[] dek = KeyGenerators.secureRandom(32).generateKey();
        byte[] encryptedPan = VaultServiceConfig.panEncryptor(dek)
                .encrypt(tokenizeRequest.pan().getBytes(StandardCharsets.UTF_8));
        byte[] encryptedDek = decEncryptor.encrypt(dek);

        VaultCard vaultCard = VaultCard
                .builder()
                .bin(bin)
                .lastFour(lastFour)
                .brand(cardBrand)
                .expiryMonth(tokenizeRequest.expiryMonth().toString())
                .expiryYear(tokenizeRequest.expiryYear().toString())
                .encryptedPan(encryptedPan)
                .encryptedDek(encryptedDek)
                .cardHolderName(tokenizeRequest.cardHolderName())
                .build();

        String token = "tok_" + RandomizerUtil.randomBase64(32);

        CardToken cardToken = CardToken
                .builder()
                .token(token)
                .merchantId(merchantId)
                .customerId(tokenizeRequest.customerId())
                .vaultCard(vaultCard)
                .build();

        vaultCard = vaultCardRepository.save(vaultCard);
        cardToken = cardTokenRepository.save(cardToken);
        return new TokenizeResponse(
                token,
                tokenizeRequest.expiryMonth(),
                tokenizeRequest.expiryYear(),
                cardBrand,
                lastFour);
    }

    /**
     * Looks up an unrevoked token, decrypts its PAN and expiry for processor routing, converts any exception into
     * {@code VAULT_CHARGE_FAILED}, and zeroes the temporary PAN byte buffer in {@code finally}. This lookup is by token
     * only and does not check token ownership against a merchant ID.
     */
    @Override
    @Transactional
    public PaymentProcessorResponse charge(UUID paymentId, String token, Money amount,
                                                          Map<String, Object> methodDetails) {
        byte[] decryptedPanBytes = null;
        try {
            CardToken cardToken = cardTokenRepository.findByTokenAndRevokedAtNull(token)
                    .orElseThrow(() -> new ResourceNotFoundException("CardToken", token));

            VaultCard vaultCard = cardToken.getVaultCard();
            byte[] encryptedDek = vaultCard.getEncryptedDek();
            byte[] decryptedDek = decEncryptor.decrypt(encryptedDek);
            decryptedPanBytes = VaultServiceConfig.panEncryptor(decryptedDek).decrypt(vaultCard.getEncryptedPan());
            String pan = new String(decryptedPanBytes, StandardCharsets.UTF_8);
            String expiry = vaultCard.getExpiryMonth() + "/" + vaultCard.getExpiryYear();

            PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest.card(paymentId, pan, expiry,
                    amount, methodDetails);

            PaymentProcessorResponse paymentProcessorResponse = cardPaymentProcessor.charge(paymentProcessorRequest);

            log.info("Vault charge registered, token={}****", token.substring(0, 4));
            return paymentProcessorResponse;
        } catch (Exception e) {
            log.error("Vault charge registered, token={}****", token.substring(0, 4));
            return new PaymentProcessorResponse.Failure("VAULT_CHARGE_FAILED", e.getMessage());
        } finally {
            if (decryptedPanBytes != null)
                Arrays.fill(decryptedPanBytes, (byte) 0);
        }
    }

    /**
     * Maps BIN prefixes 4, 5/2, and 37/34 to Visa, Mastercard, and Amex respectively; all other prefixes map to RuPay.
     */
    private CardBrand detectBrand(String ch) {
        if (ch.startsWith("4"))
            return CardBrand.VISA;
        else if (ch.startsWith("5") || ch.startsWith("2"))
            return CardBrand.MASTERCARD;
        else if (ch.startsWith("37") || ch.startsWith("34"))
            return CardBrand.AMEX;
        return CardBrand.RUPAY;
    }
}
