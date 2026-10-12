package common_lib.util;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

public class SignerUtil {

    private static final String ALGO = "HmacSHA256";

    /**
     * Computes the configured HMAC digest over the UTF-8 payload using the UTF-8 secret and returns lowercase hex.
     *
     * @param payload exact text to sign
     * @param secret shared signing secret
     * @return lowercase hexadecimal HMAC digest
     * @throws RuntimeException if the HMAC algorithm or key cannot be initialized
     */
    public String sign(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance(ALGO);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGO));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException("HMAC signing failed", e);
        }
    }
}
