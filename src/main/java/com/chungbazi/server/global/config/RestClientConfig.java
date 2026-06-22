package com.chungbazi.server.global.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    private static final String YOUTH_POLICY_BASE_URL = "https://www.youthcenter.go.kr";
    private static final int CONNECT_TIMEOUT_MILLISECONDS = 5000;
    private static final int READ_TIMEOUT_MILLISECONDS = 5000;

    @Bean
    public RestClient restClient() {
        return baseRestClientBuilder().build();
    }

    @Bean
    public RestClient youthPolicyRestClient() {
        return baseRestClientBuilder()
                .baseUrl(YOUTH_POLICY_BASE_URL)
                .build();
    }

    private RestClient.Builder baseRestClientBuilder() {
        return RestClient.builder()
                .requestFactory(clientHttpRequestFactory())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    private SimpleClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MILLISECONDS);
        factory.setReadTimeout(READ_TIMEOUT_MILLISECONDS);

        return factory;
    }
}
