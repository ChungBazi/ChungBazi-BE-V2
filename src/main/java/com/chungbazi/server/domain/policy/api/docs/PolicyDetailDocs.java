package com.chungbazi.server.domain.policy.api.docs;

import com.chungbazi.server.domain.policy.api.dto.response.PolicyCardResponse;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.resolver.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "[Policy Detail]", description = "정책 조회 관련 API")
public interface PolicyDetailDocs {

    @Operation(
            summary = "정책 카드뉴스 조회 API",
            description = """
                    정책 카드뉴스 정보를 조회하는 API입니다.
                    ### Query Parameter
                    ---
                    - `policyId`: 청바지 서비스 내의 정책 id
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정책 카드뉴스 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "정책을 찾을 수 없음")
    })
    CommonResponse<PolicyCardResponse> getPolicyCard(
            @CurrentUser User user,
            @Parameter(description = "정책 id", example = "1", required = true)
            @RequestParam Long policyId
    );
}
