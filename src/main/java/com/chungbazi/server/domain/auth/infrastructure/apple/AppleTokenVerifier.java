package com.chungbazi.server.domain.auth.infrastructure.apple;

import com.chungbazi.server.global.common.code.exception.GeneralException;
import com.chungbazi.server.global.common.code.status.ErrorStatus;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class AppleTokenVerifier {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";
    private static final String[] DEFAULT_APPLE_USER_NAME_PREFIXES = {
            "든든한", "야무진", "똑똑한", "부지런한", "반가운",
            "성실한", "희망찬", "알찬", "힘찬", "빛나는"
    };
    private static final String DEFAULT_APPLE_USER_NAME = "바로";

    private final AppleProperties appleProperties;

    public AppleUserInfo verify(String idToken, String requestEmail, String requestName) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(idToken);

            RSAKey rsaKey = getMatchedRsaKey(signedJWT);
            RSAPublicKey publicKey = rsaKey.toRSAPublicKey();

            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            if (!signedJWT.verify(verifier)) {
                throw new GeneralException(ErrorStatus._INVALID_TOKEN);
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            validateClaims(claims);

            String providerId = claims.getSubject();
            if (providerId == null || providerId.isBlank()) {
                throw new GeneralException(ErrorStatus._INVALID_TOKEN);
            }

            String email = resolveAppleEmail(claims.getStringClaim("email"), requestEmail);
            String name = resolveAppleName(requestName);

            return new AppleUserInfo(providerId, email, name);
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorStatus._INVALID_TOKEN);
        }
    }
    private RSAKey getMatchedRsaKey(SignedJWT signedJWT) throws Exception {
        JWKSet jwkSet = JWKSet.load(new URL(APPLE_KEYS_URL));
        JWK jwk = jwkSet.getKeyByKeyId(signedJWT.getHeader().getKeyID());

        if (!(jwk instanceof RSAKey rsaKey)) {
            throw new GeneralException(ErrorStatus._INVALID_TOKEN);
        }

        return rsaKey;
    }

    private void validateClaims(JWTClaimsSet claims) {
        if (!APPLE_ISSUER.equals(claims.getIssuer())) {
            throw new GeneralException(ErrorStatus._INVALID_TOKEN);
        }

        if (!claims.getAudience().contains(appleProperties.audience())) {
            throw new GeneralException(ErrorStatus._INVALID_TOKEN);
        }

        Date expirationTime = claims.getExpirationTime();
        if (expirationTime == null || expirationTime.before(new Date())) {
            throw new GeneralException(ErrorStatus._EXPIRED_TOKEN);
        }
    }

    private String resolveAppleEmail(String tokenEmail, String requestEmail) {
        if (tokenEmail != null && !tokenEmail.isBlank()) {
            return tokenEmail;
        }

        if (requestEmail != null && !requestEmail.isBlank()) {
            return requestEmail;
        }

        throw new GeneralException(ErrorStatus._INVALID_TOKEN);
    }

    private String resolveAppleName(String requestName) {
        if (requestName != null && !requestName.isBlank()) {
            return requestName;
        }

        return generateDefaultAppleName();
    }

    private String generateDefaultAppleName() {
        String prefix = DEFAULT_APPLE_USER_NAME_PREFIXES[
                ThreadLocalRandom.current().nextInt(DEFAULT_APPLE_USER_NAME_PREFIXES.length)
                ];
        return prefix + " " + DEFAULT_APPLE_USER_NAME;
    }
}
