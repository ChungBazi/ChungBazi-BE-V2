package com.chungbazi.server.global.config;

import com.chungbazi.server.domain.auth.infrastructure.apple.AppleProperties;
import com.chungbazi.server.global.security.jwt.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(value = {
        JwtProperties.class,
        AppleProperties.class
})
public class ConfigurationPropertiesConfig {
}
