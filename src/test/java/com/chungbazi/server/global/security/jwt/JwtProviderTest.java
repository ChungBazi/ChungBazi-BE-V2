package com.chungbazi.server.global.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.chungbazi.server.domain.auth.exception.AuthException;
import io.jsonwebtoken.Claims;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

    private static final String SECRET = "test-secret-key-test-secret-key-test-secret-key-test-secret-key";
    private static final long ACCESS_EXPIRATION = 60_000L;
    private static final long REFRESH_EXPIRATION = 120_000L;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

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

    @Test
    @SuppressWarnings("DataFlowIssue")
    void rejectsBlankJwtStringPropertiesAndNonPositiveExpirations() {
        JwtProperties properties = new JwtProperties(
                " ",
                " ",
                "",
                0L,
                -1L
        );

        Set<String> invalidProperties = validator.validate(properties).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(java.util.stream.Collectors.toSet());

        assertThat(invalidProperties).containsExactlyInAnyOrder(
                "secret",
                "issuer",
                "audience",
                "accessExp",
                "refreshExp"
        );
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    void rejectsNullExpirationProperties() {
        JwtProperties properties = new JwtProperties(
                SECRET,
                "prod",
                "prod-server",
                null,
                null
        );

        Set<String> invalidProperties = validator.validate(properties).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(java.util.stream.Collectors.toSet());

        assertThat(invalidProperties).containsExactlyInAnyOrder("accessExp", "refreshExp");
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
