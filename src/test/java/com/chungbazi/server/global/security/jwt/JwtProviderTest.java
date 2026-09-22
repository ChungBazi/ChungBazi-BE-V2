package com.chungbazi.server.global.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chungbazi.server.domain.auth.exception.AuthException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

    private static final String SECRET = "test-secret-key-test-secret-key-test-secret-key-test-secret-key";
    private static final long ACCESS_EXPIRATION = 60_000L;
    private static final long REFRESH_EXPIRATION = 120_000L;

    @Test
    void createsAccessTokenWithConfiguredIssuerAndAudience() {
        JwtProvider provider = provider("prod", "prod-server");

        String token = provider.createAccessToken(15L);
        Claims claims = provider.getClaims(token);

        assertThat(provider.validateToken(token)).isTrue();
        assertThat(claims.getSubject()).isEqualTo("15");
        assertThat(claims.getIssuer()).isEqualTo("prod");
        assertThat(claims.getAudience()).isEqualTo("prod-server");
    }

    @Test
    void createsRefreshTokenWithConfiguredIssuerAndAudience() {
        JwtProvider provider = provider("prod", "prod-server");

        String token = provider.createRefreshToken(15L);
        Claims claims = provider.getClaims(token);

        assertThat(provider.validateToken(token)).isTrue();
        assertThat(claims.getSubject()).isEqualTo("15");
        assertThat(claims.getIssuer()).isEqualTo("prod");
        assertThat(claims.getAudience()).isEqualTo("prod-server");
    }

    @Test
    void rejectsTokenIssuedForDifferentEnvironment() {
        JwtProvider devProvider = provider("dev", "dev-server");
        JwtProvider prodProvider = provider("prod", "prod-server");
        String devToken = devProvider.createAccessToken(15L);

        assertThatThrownBy(() -> prodProvider.validateToken(devToken))
                .isInstanceOf(AuthException.class);
    }

    @Test
    void rejectsTokenWithDifferentAudienceEvenWhenIssuerAndSecretMatch() {
        JwtProvider issuer = provider("prod", "another-service");
        JwtProvider prodProvider = provider("prod", "prod-server");
        String tokenForAnotherService = issuer.createAccessToken(15L);

        assertThatThrownBy(() -> prodProvider.validateToken(tokenForAnotherService))
                .isInstanceOf(AuthException.class);
    }

    private JwtProvider provider(String issuer, String audience) {
        return new JwtProvider(new JwtProperties(
                SECRET,
                issuer,
                audience,
                ACCESS_EXPIRATION,
                REFRESH_EXPIRATION
        ));
    }
}
