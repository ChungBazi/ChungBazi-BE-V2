package com.chungbazi.server.domain.policy.client;

import com.chungbazi.server.domain.policy.client.dto.YouthPolicyListResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class YouthPolicyClient {

    private static final String POLICY_LIST_PATH = "/go/ythip/getPlcy";
    private static final String API_KEY_PARAM_NAME = "apiKeyNm";
    private static final String PAGE_PARAM_NAME = "pageNum";
    private static final String SIZE_PARAM_NAME = "pageSize";

    private final RestClient restClient;
    private final String apiKey;

    public YouthPolicyClient(
            @Qualifier("youthPolicyRestClient") RestClient restClient,
            @Value("${external.youth-policy.api-key:}") String apiKey
    ) {
        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    public YouthPolicyListResponse fetchPolicies(int pageNum, int pageSize) {
        return restClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(POLICY_LIST_PATH)
                            .queryParam(PAGE_PARAM_NAME, pageNum)
                            .queryParam(SIZE_PARAM_NAME, pageSize)
                            .queryParam("rtnType", "json");

                    if (apiKey != null && !apiKey.isBlank()) {
                        uriBuilder.queryParam(API_KEY_PARAM_NAME, apiKey);
                    }

                    return uriBuilder.build();
                })
                .retrieve()
                .body(YouthPolicyListResponse.class);
    }
}
