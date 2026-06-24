package com.chungbazi.server.domain.auth.infrastructure.apple;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "apple")
public record AppleProperties(
        @NotBlank
        String audience
) {
}
