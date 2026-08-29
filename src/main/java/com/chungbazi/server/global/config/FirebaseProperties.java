package com.chungbazi.server.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "firebase")
public record FirebaseProperties(
        boolean enabled,
        String serviceAccountPath
) {
}
