package common_lib.util;

import common_lib.context.MerchantContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.web.context.annotation.RequestScope;

import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@AutoConfiguration
public class SharedSecurityAutoConfiguration {

    @Bean
    public SignerUtil signerUtil() {
        return new SignerUtil();
    }

    /**
     * Builds an AES-GCM encryptor from the configured Base64-encoded AES master key.
     *
     * <p>The decoded key is used only to construct the encryptor; each encryption operation receives a fresh
     * 12-byte cryptographically secure nonce from {@link KeyGenerators#secureRandom(int)}. The master key and
     * plaintext secrets are not returned or logged by this method.
     *
     * @return a non-null encryptor that encrypts and decrypts protected bytes with AES-GCM
     */
    // This bean will only be created in the service where vault.master-key property is available.
    @Bean
    @ConditionalOnProperty(name = "vault.master-key")
    public BytesEncryptor masterKeyEncryptor(@Value("${vault.master-key}") String masterKey) {
        byte[] masterKeyBytes = Base64.getDecoder().decode(masterKey);
        SecretKeySpec decKey = new SecretKeySpec(masterKeyBytes, "AES");   //Use masterKeyBytes to encrypt using AES algo
        return new AesBytesEncryptor(decKey,
                KeyGenerators.secureRandom(12), AesBytesEncryptor.CipherAlgorithm.GCM);
    }

    @Bean
    @ConditionalOnProperty(name = "webhoook.secret-encryption-key")
    public BytesEncryptor webhookSecretEncryptor(@Value("${webhoook.secret-encryption-key}") String secretKey) {
        byte[] masterKeyBytes = Base64.getDecoder().decode(secretKey);
        SecretKeySpec decKey = new SecretKeySpec(masterKeyBytes, "AES");   //Use masterKeyBytes to encrypt using AES algo
        return new AesBytesEncryptor(decKey,
                KeyGenerators.secureRandom(12), AesBytesEncryptor.CipherAlgorithm.GCM);
    }

    @Bean
    @RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
    public MerchantContext merchantContext() {
        return new MerchantContext();
    }
}
