package com.chungbazi.server.domain.policy.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YouthPolicyListResponse(
        Integer resultCode,
        String resultMessage,
        Result result
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            Paging pagging,
            List<YouthPolicyItem> youthPolicyList
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Paging(
            Integer totCount,
            Integer pageNum,
            Integer pageSize
    ) {
    }
}
