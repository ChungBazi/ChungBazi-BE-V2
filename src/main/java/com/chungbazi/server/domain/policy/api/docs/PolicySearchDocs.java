package com.chungbazi.server.domain.policy.api.docs;

import com.chungbazi.server.domain.policy.api.dto.request.SearchKeywordAutoSaveRequest;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse;
import com.chungbazi.server.domain.policy.api.dto.response.RecentSearchKeywordListResponse;
import com.chungbazi.server.domain.policy.api.dto.response.SearchSuggestionResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "[Policy Search]", description = "정책 검색 관련 API")
public interface PolicySearchDocs {
    @Operation(
            summary = "정책 검색 결과 조회 API",
            description = """
                    ### Query Parameter
                    ---
                    - `keyword`: 검색어
                    - `category`: 정책 분야. 생략하면 전체 분야에서 검색
                    - `sort`: 정렬 기준. `LATEST`는 최신순, `DEADLINE`은 마감순
                    - `cursor`: 이전 응답에서 받은 다음 페이지 커서. 최초 요청에서는 생략
                    - `size`: 한 번에 조회할 정책 수. 기본 20, 최대 50

                    ### ResponseBody
                    ---
                    - `totalCount`: 검색 조건에 해당하는 전체 정책 수
                    - `policies`: 조회된 정책 목록
                    - `nextCursor`: 다음 페이지 조회 커서. 다음 페이지가 없으면 null
                    - `hasNext`: 다음 페이지 존재 여부
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정책 검색 결과 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 검색어, 카테고리, 정렬 기준 또는 커서"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    CommonResponse<PolicyListResponse> searchPolicies(
            @CurrentUser User user,
            @Parameter(description = "검색어", example = "청년 월세", required = true)
            @RequestParam String keyword,
            @Parameter(description = "정책 분야. 생략하면 전체 분야 검색", example = "HOUSING")
            @RequestParam(required = false) PolicyCategoryType category,
            @Parameter(description = "정렬 기준", example = "LATEST")
            @RequestParam(defaultValue = "LATEST") PolicySortType sort,
            @Parameter(description = "이전 응답에서 받은 다음 페이지 커서")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "조회 개수", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    );

    @Operation(
            summary = "정책 검색어 자동완성 조회 API",
            description = """
                    ### Query Parameter
                    ---
                    - `keyword`: 자동완성 기준 검색어

                    ### ResponseBody
                    ---
                    - `keywords`: 검색어를 포함하는 정책 제목 자동완성 목록. 최신 정책 기준 최대 5개 반환
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정책 검색어 자동완성 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 검색어"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    CommonResponse<SearchSuggestionResponse> getSearchSuggestions(
            @CurrentUser User user,
            @Parameter(description = "자동완성 기준 검색어", example = "청년", required = true)
            @RequestParam String keyword
    );

    @Operation(
            summary = "최근 검색어 자동 저장 설정 변경 API",
            description = """
                    ### RequestBody
                    ---
                    - `enabled`: 최근 검색어 자동 저장 여부
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "최근 검색어 자동 저장 설정 변경 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    CommonResponse<String> updateSearchKeywordAutoSaveEnabled(
            @CurrentUser User user,
            @RequestBody SearchKeywordAutoSaveRequest request
    );

    @Operation(
            summary = "최근 검색어 목록 조회 API",
            description = """
                    ### ResponseBody
                    ---
                    - `autoSaveEnabled`: 최근 검색어 자동 저장 여부
                    - `keywords`: 최근 검색어 목록. 최근 검색 시각 기준 내림차순으로 반환
                    - `nextCursor`: 다음 페이지 조회 커서. 다음 페이지가 없으면 null
                    - `hasNext`: 다음 페이지 존재 여부
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "최근 검색어 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    CommonResponse<RecentSearchKeywordListResponse> getRecentSearchKeywords(
            @CurrentUser User user,
            @Parameter(description = "이전 응답에서 받은 다음 페이지 커서")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "조회 개수", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    );

    @Operation(
            summary = "최근 검색어 단건 삭제 API",
            description = """
                    ### Path Variable
                    ---
                    - `keywordId`: 삭제할 최근 검색어 ID
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "최근 검색어 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "최근 검색어를 찾을 수 없음")
    })
    CommonResponse<String> deleteRecentSearchKeyword(
            @CurrentUser User user,
            @Parameter(description = "삭제할 최근 검색어 ID", example = "1")
            @PathVariable Long keywordId
    );

    @Operation(
            summary = "최근 검색어 전체 삭제 API",
            description = "현재 사용자의 최근 검색어를 모두 삭제"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "최근 검색어 전체 삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    CommonResponse<String> deleteAllRecentSearchKeywords(
            @CurrentUser User user
    );
}
