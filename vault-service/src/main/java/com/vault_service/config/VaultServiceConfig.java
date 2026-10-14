package com.vault_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

import javax.crypto.spec.SecretKeySpec;

/**
 * Configures AES-GCM encryption for vaulted payment-card data.
 */
@Configuration
public class VaultServiceConfig {

    @Value("${vault.service.master-key}")
    private String masterKey;


    /**
     * Creates an AES-GCM encryptor for PAN bytes using the per-card data-encryption key and a secure nonce generator.
     *
     * <p>The PAN is encrypted, not logged or returned; callers must clear plaintext buffers after use.
     *
     * @param dek per-card AES data-encryption key
     * @return encryptor that uses AES-GCM with fresh secure 12-byte nonces
     */
    public static BytesEncryptor panEncryptor(byte[] dek) {
        SecretKeySpec decKey = new SecretKeySpec(dek, "AES");   //Use dek to encrypt by using AES algo
        return new AesBytesEncryptor(decKey,
                KeyGenerators.secureRandom(12), AesBytesEncryptor.CipherAlgorithm.GCM);

    }
}
