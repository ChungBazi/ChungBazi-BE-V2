package com.chungbazi.server.global.config;

import com.chungbazi.server.domain.policy.client.YouthPolicyProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(YouthPolicyProperties.class)
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
