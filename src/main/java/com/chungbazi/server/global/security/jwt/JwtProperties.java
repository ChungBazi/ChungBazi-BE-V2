package com.chungbazi.server.global.security.jwt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        @NotBlank
        String secret,

        @NotBlank
        String issuer,

        @NotBlank
        String audience,

        @NotNull @Positive
        Long accessExp,

        @NotNull @Positive
        Long refreshExp
) {
}
