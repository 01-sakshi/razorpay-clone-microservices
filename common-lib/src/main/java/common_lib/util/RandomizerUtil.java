package common_lib.util;

import java.security.SecureRandom;
import java.util.Base64;

public class RandomizerUtil {

    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Encodes cryptographically secure random bytes as unpadded URL-safe Base64; the encoded length can exceed
     * {@code length} because the argument is the byte count, not a requested character count.
     *
     * @param length number of random bytes to generate
     * @return URL-safe Base64 encoding without padding
     */
    public static String randomBase64(int length) {
        byte[] buf = new byte[length];
        secureRandom.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
