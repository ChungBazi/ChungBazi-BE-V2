package com.chungbazi.server.domain.auth.infrastructure.kakao;

import com.chungbazi.server.global.common.code.exception.GeneralException;
import com.chungbazi.server.global.common.code.status.ErrorStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class KakaoClient {

    private static final String KAKAO_USER_INFO_PATH = "/v2/user/me";
    private final RestClient restClient;

    public KakaoClient(@Qualifier("kakaoRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public KakaoUserInfo getUserInfo(String accessToken) {
        try {
            Map<String, Object> response = restClient.get()
                    .uri(KAKAO_USER_INFO_PATH)
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (response == null) {
                throw new GeneralException(ErrorStatus._KAKAO_API_ERROR);
            }
            return new KakaoUserInfo(response);
        } catch (RestClientException e) {
            throw new GeneralException(ErrorStatus._KAKAO_API_ERROR);
        }
    }
}
