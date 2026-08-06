package com.chungbazi.server.domain.policy.api.dto.response;

import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse.PolicySummary;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "마감이 다가오는 찜한 정책 리스트 조회 응답")
public record MyPolicyDeadlineResponse(

        @Schema(description = "정책 목록")
        List<PolicySummary> policies
) {
}
