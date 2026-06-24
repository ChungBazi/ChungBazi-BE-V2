package com.chungbazi.server.domain.auth.infrastructure.apple;

import com.chungbazi.server.domain.auth.exception.AuthException;
import com.chungbazi.server.domain.auth.exception.code.AuthErrorCode;
import com.chungbazi.server.global.common.code.exception.GeneralException;
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

@Component
@RequiredArgsConstructor
public class AppleTokenVerifier {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";

    private final AppleProperties appleProperties;

    public AppleTokenInfo verify(String idToken) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(idToken);

            RSAKey rsaKey = getMatchedRsaKey(signedJWT);
            RSAPublicKey publicKey = rsaKey.toRSAPublicKey();

            JWSVerifier verifier = new RSASSAVerifier(publicKey);
            if (!signedJWT.verify(verifier)) {
                throw new AuthException(AuthErrorCode.INVALID_TOKEN);
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            validateClaims(claims);

            String providerId = claims.getSubject();
            if (providerId == null || providerId.isBlank()) {
                throw new AuthException(AuthErrorCode.INVALID_TOKEN);
            }
            String email = claims.getStringClaim("email");

            return AppleTokenInfo.of(providerId, email);
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }
    }

    // TODO: Apple JWK Set 캐싱 적용
    private RSAKey getMatchedRsaKey(SignedJWT signedJWT) throws Exception {
        JWKSet jwkSet = JWKSet.load(new URL(APPLE_KEYS_URL));
        JWK jwk = jwkSet.getKeyByKeyId(signedJWT.getHeader().getKeyID());

        if (!(jwk instanceof RSAKey rsaKey)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        return rsaKey;
    }

    private void validateClaims(JWTClaimsSet claims) {
        if (!APPLE_ISSUER.equals(claims.getIssuer())) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        if (!claims.getAudience().contains(appleProperties.audience())) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        Date expirationTime = claims.getExpirationTime();
        if (expirationTime == null || expirationTime.before(new Date())) {
            throw new AuthException(AuthErrorCode.EXPIRED_TOKEN);
        }
    }
}
