package common_lib.config;

import java.util.Base64;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

/**
 * Configures the encryptor used to protect vault and other persisted secret bytes.
 */
//@Configuration
public class AesEncryptionConfig {

    @Value("${vault.service.master-key}")
    private String masterKey;
    
    /**
        * Builds an AES-GCM encryptor from the configured Base64-encoded AES master key.
        *
        * <p>The decoded key is used only to construct the encryptor; each encryption operation receives a fresh
        * 12-byte cryptographically secure nonce from {@link KeyGenerators#secureRandom(int)}. The master key and
        * plaintext secrets are not returned or logged by this method.
     *
        * @return a non-null encryptor that encrypts and decrypts protected bytes with AES-GCM
     */
    @Bean 
    public BytesEncryptor masterKeyEncryptor() {
        byte[] masterKeyBytes = Base64.getDecoder().decode(masterKey);
        SecretKeySpec decKey = new SecretKeySpec(masterKeyBytes, "AES");   //Use masterKeyBytes to encrypt using AES algo
        return new AesBytesEncryptor(decKey,
                KeyGenerators.secureRandom(12), AesBytesEncryptor.CipherAlgorithm.GCM);
    }
}
