package com.chungbazi.server.domain.policy.api.docs;

import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.PolicySortType;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.resolver.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "[Home]", description = "홈 정책 관련 API")
public interface HomeDocs {

    @Operation(
            summary = "분야별 정책 목록 조회 API",
            description = """
                    선택한 정책 분야의 정책을 커서 기반 무한스크롤로 조회합니다.

                    - `category`: 정책 분야
                    - `sort`: `LATEST`(최신순), `DEADLINE`(마감순)
                    - `cursor`: 최초 요청에서는 생략하고, 다음 요청부터 응답의 `nextCursor`를 전달합니다.
                    - `size`: 한 번에 조회할 정책 수(기본 20, 최대 50)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "분야별 정책 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 카테고리, 정렬 또는 커서"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    CommonResponse<PolicyListResponse> getPoliciesByCategory(
            @CurrentUser User user,
            @Parameter(description = "정책 분야", example = "JOB_STARTUP", required = true)
            @RequestParam PolicyCategoryType category,
            @Parameter(description = "정렬 기준", example = "LATEST")
            @RequestParam(defaultValue = "LATEST") PolicySortType sort,
            @Parameter(description = "이전 응답에서 받은 다음 페이지 커서")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "조회 개수", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    );

    @Operation(
            summary = "최신순 정책 목록 조회 API",
            description = """
                    최신 등록 정책을 커서 기반 무한스크롤로 조회합니다.

                    - `category`: 선택 정책 분야. 생략하면 전체 분야를 조회합니다.
                    - `cursor`: 최초 요청에서는 생략하고, 다음 요청부터 응답의 `nextCursor`를 전달합니다.
                    - `size`: 한 번에 조회할 정책 수(기본 20, 최대 50)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "최신순 정책 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 카테고리 또는 커서"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    CommonResponse<PolicyListResponse> getLatestPolicies(
            @CurrentUser User user,
            @Parameter(description = "정책 분야. 생략하면 전체 분야 조회", example = "JOB_STARTUP")
            @RequestParam(required = false) PolicyCategoryType category,
            @Parameter(description = "이전 응답에서 받은 다음 페이지 커서")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "조회 개수", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    );
}
