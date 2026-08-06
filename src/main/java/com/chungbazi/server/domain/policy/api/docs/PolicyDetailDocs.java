package com.chungbazi.server.domain.policy.api.docs;

import com.chungbazi.server.domain.policy.api.dto.response.PolicyCardResponse;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyDetailResponse;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.resolver.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "[Policy Detail]", description = "정책 상세 조회 / 찜 관련 API")
public interface PolicyDetailDocs {

    @Operation(
            summary = "정책 카드뉴스 조회 API",
            description = """
                    정책 카드뉴스 정보를 조회하는 API입니다.
                    ### Path Variables
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
            @PathVariable Long policyId
    );

    @Operation(
            summary = "정책 상세 조회 API",
            description = """
                    정책 정보를 상세 조회하는 API입니다.
                    ### Path Variables
                    ---
                    - `policyId`: 청바지 서비스 내의 정책 id

                    ### ResponseBody
                    ---
                    - 본문 영역은 7개 필드로 반환합니다.
                    - `policies`: 맞춤 추천 정책 목록. 추천 로직 구현 전까지 null로 반환합니다.
                    - `popularPolicies`: 같은 분야의 인기 정책 목록
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정책 상세 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "정책을 찾을 수 없음")
    })
    CommonResponse<PolicyDetailResponse> getPolicyDetail(
            @CurrentUser User user,
            @Parameter(description = "정책 id", example = "1", required = true)
            @PathVariable Long policyId
    );

    @Operation(
            summary = "정책 찜 API",
            description = """
                    정책을 현재 사용자의 찜 목록에 추가하는 API입니다.
                    이미 찜한 정책이면 추가 작업 없이 성공 응답을 반환합니다.
                    ### Path Variables
                    ---
                    - `policyId`: 청바지 서비스 내의 정책 id
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정책 찜 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "정책을 찾을 수 없음")
    })
    CommonResponse<String> likePolicy(
            @CurrentUser User user,
            @Parameter(description = "정책 id", example = "1", required = true)
            @PathVariable Long policyId
    );

    @Operation(
            summary = "정책 찜 취소 API",
            description = """
                    정책을 현재 사용자의 찜 목록에서 제거하는 API입니다.
                    이미 찜하지 않은 정책이면 추가 작업 없이 성공 응답을 반환합니다.
                    ### Path Variables
                    ---
                    - `policyId`: 청바지 서비스 내의 정책 id
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정책 찜 취소 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    CommonResponse<String> unlikePolicy(
            @CurrentUser User user,
            @Parameter(description = "정책 id", example = "1", required = true)
            @PathVariable Long policyId
    );
}
