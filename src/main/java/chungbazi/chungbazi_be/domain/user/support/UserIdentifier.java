package chungbazi.chungbazi_be.domain.user.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class UserIdentifier {

    @Value("${logging.salt-key}")
    private static String saltKey;

    @Value("${logging.salt-key}")
    public void setSaltKey(String saltKey) {
        UserIdentifier.saltKey = saltKey;
    }

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
