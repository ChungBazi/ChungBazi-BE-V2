package com.chungbazi.server.domain.policy.api.docs;

import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse;
import com.chungbazi.server.domain.policy.api.dto.response.HomePolicyResponse;
import com.chungbazi.server.domain.policy.api.dto.response.PersonalizedPolicyResponse;
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
            summary = "홈 화면 정책 섹션 조회 API",
            description = """
                    홈 화면에 필요한 정책 섹션을 한 번에 조회합니다.
                    각 섹션은 최대 5개 정책을 반환합니다.
                    
                    - 맞춤 정책: 사용자 온보딩, 행동 데이터 기반 추천 순
                    - 최근 본 정책: 현재 사용자가 최근 조회한 순서
                    - 인기 정책: 인기도 순
                    - 마감 임박 정책: 마감일이 가까운 순
                    - 최신 정책: 최신 등록 순
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "홈 화면 정책 섹션 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    CommonResponse<HomePolicyResponse> getHomePolicies(
            @CurrentUser User user
    );

    @Operation(
            summary = "분야별 맞춤 정책 목록 조회 API",
            description = """
                    전국 정책과 현재 사용자의 지역에 해당하는 정책 중에서,
                    선택한 정책 분야의 맞춤 정책을 추천순으로 최대 5개 조회합니다.
                    사용자가 관심 분야로 선택하지 않은 정책 분야는 빈 목록을 반환합니다.

                    - `category`: 정책 분야
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "분야별 맞춤 정책 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 카테고리"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    CommonResponse<PersonalizedPolicyResponse> getPersonalizedPoliciesByCategory(
            @CurrentUser User user,
            @Parameter(description = "정책 분야", example = "JOB_STARTUP", required = true)
            @RequestParam PolicyCategoryType category
    );

    @Operation(
            summary = "최근 본 정책 목록 조회 API",
            description = """
                    현재 사용자가 최근 본 정책을 최근 조회한 순서대로 조회합니다.
                    같은 정책을 여러 번 본 경우 가장 최근 조회 기록을 기준으로 한 번만 노출합니다.

                    - `cursor`: 최초 요청에서는 생략하고, 다음 요청부터 응답의 `nextCursor`를 전달합니다.
                    - `size`: 조회할 정책 수(기본 5, 최대 5)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "최근 본 정책 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    CommonResponse<PolicyListResponse> getRecentViewedPolicies(
            @CurrentUser User user,
            @Parameter(description = "이전 응답에서 받은 다음 페이지 커서")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "조회 개수", example = "5")
            @RequestParam(defaultValue = "5") @Min(1) @Max(5) int size
    );

    @Operation(
            summary = "분야별 정책 목록 조회 API",
            description = """
                    전국 정책과 현재 사용자의 지역에 해당하는 정책 중에서,
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
                    전국 정책과 현재 사용자의 지역에 해당하는 최신 등록 정책을
                    커서 기반 무한스크롤로 조회합니다.

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

    @Operation(
            summary = "마감 임박 정책 목록 조회 API",
            description = """
                    전국 정책과 현재 사용자의 지역에 해당하는 정책 중에서,
                    마감일이 가까운 정책을 커서 기반 무한스크롤로 조회합니다.
                    마감일이 없는 상시 정책과 이미 마감된 정책은 제외합니다.

                    - `category`: 선택 정책 분야. 생략하면 전체 분야를 조회합니다.
                    - `cursor`: 최초 요청에서는 생략하고, 다음 요청부터 응답의 `nextCursor`를 전달합니다.
                    - `size`: 한 번에 조회할 정책 수(기본 20, 최대 50)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "마감 임박 정책 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 카테고리 또는 커서"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    CommonResponse<PolicyListResponse> getUpcomingDeadlinePolicies(
            @CurrentUser User user,
            @Parameter(description = "정책 분야. 생략하면 전체 분야 조회", example = "JOB_STARTUP")
            @RequestParam(required = false) PolicyCategoryType category,
            @Parameter(description = "이전 응답에서 받은 다음 페이지 커서")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "조회 개수", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    );

    @Operation(
            summary = "인기 정책 목록 조회 API",
            description = """
                    전국 정책과 현재 사용자의 지역에 해당하는 정책 중에서,
                    인기있는 정책을 커서 기반 무한스크롤로 조회합니다.
                    만약 가중치가 같은 경우, 최신 정책을 먼저 상단에 노출시킵니다.
                    
                    - `category` : 선택 정책 분야, 생략하면 전체 분야를 조회
                    - `cursor` : 최초 요청에서는 생략하고, 다음 요청부터는 응답의 `nextCursor`를 전달
                    - `size` : 한 번에 조회할 정책 수(기본 20, 최대 50)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인기 정책 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 카테고리 또는 커서"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    CommonResponse<PolicyListResponse> getPopularPolicies(
            @CurrentUser User user,
            @Parameter(description = "정책 분야, 생략하면 전체 분야 조회", example = "JOB_STARTUP")
            @RequestParam(required = false) PolicyCategoryType category,
            @Parameter(description = "이전 응답에서 받은 다음 페이지 커서")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "조회 개수", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    );
}
