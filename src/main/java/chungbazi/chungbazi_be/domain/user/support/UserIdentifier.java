package chungbazi.chungbazi_be.domain.user.support;

import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

public class UserIdentifier {

    @Value("${logging.salt-key}")
    private static String saltKey;

    public static String hashUserId(Long userId) {
        if (userId == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String rawInput = userId + saltKey;
            byte[] encodedHash = digest.digest(rawInput.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encodedHash);
        } catch (Exception e) {
            return null;
        }
    }
}
