package com.chungbazi.server.domain.auth.infrastructure.apple;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "apple")
public record AppleProperties(
        String audience
) {
}
