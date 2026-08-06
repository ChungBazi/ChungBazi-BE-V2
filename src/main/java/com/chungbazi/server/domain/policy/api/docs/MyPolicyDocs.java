package com.chungbazi.server.domain.policy.api.docs;

import com.chungbazi.server.domain.policy.api.dto.response.MyPolicyDeadlineResponse;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.resolver.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "[My Policy]", description = "내 정책 관련 API")
public interface MyPolicyDocs {

    @Operation(
            summary = "마감이 다가오는 찜한 정책 조회 API",
            description = """
            내 정책 조회 시, 상단에 마감이 다가오는 찜한 정책을 조회하는 API 입니다. \n
            마감 잔여일이 적은 순이며, 최대 5개 정책을 반환합니다.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "마감이 다가오는 찜한 정책 조회 성공"),
    })
    CommonResponse<MyPolicyDeadlineResponse> getMyPolicyDeadline(@CurrentUser User user);
}
