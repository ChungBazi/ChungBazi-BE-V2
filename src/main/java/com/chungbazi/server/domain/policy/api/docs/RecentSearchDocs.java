package com.chungbazi.server.domain.policy.api.docs;

import com.chungbazi.server.domain.policy.api.dto.request.SearchKeywordAutoSaveRequest;
import com.chungbazi.server.domain.policy.api.dto.response.RecentSearchKeywordListResponse;
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

@Tag(name = "[Recent Search]", description = "최근 검색어 관련 API")
public interface RecentSearchDocs {

    @Operation(
            summary = "최근 검색어 목록 조회 API",
            description = """
                    ### Query Parameter
                    ---
                    - `cursor`: 이전 응답에서 받은 다음 페이지 커서. 최초 요청에서는 생략
                    - `size`: 한 번에 조회할 최근 검색어 수. 기본 20, 최대 50

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
}
