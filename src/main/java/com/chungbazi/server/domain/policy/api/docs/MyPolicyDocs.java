package com.chungbazi.server.domain.policy.api.docs;

import com.chungbazi.server.domain.policy.api.dto.response.CalendarResponse;
import com.chungbazi.server.domain.policy.api.dto.request.PolicyMemoRequest;
import com.chungbazi.server.domain.policy.api.dto.response.MyPolicyDeadlineResponse;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyMemoResponse;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.PolicyListSortType;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.resolver.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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

    @Operation(
            summary = "현재 날짜에 마감되는 정책 조회 API",
            description = """
                    사용자가 확인하고 싶은 날짜의 찜한 정책을 조회하는 API입니다. \n
                    해당 날짜에 마감되는 정책을 최근 찜한 순으로 전체 조회합니다.

                    ### Query Parameter
                    - `targetDate`: 사용자가 확인하고 싶은 날짜로, YYYY-MM-DD 형식입니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "현재 날짜에 마감되는 찜한 정책 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 날짜"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    CommonResponse<PolicyListResponse> getDeadlinePoliciesByDate(
            @CurrentUser User user,
            @Parameter(description = "사용자가 확인하고 싶은 날짜", example = "2026-08-06", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate
    );

    @Operation(
            summary = "찜한 상시 정책 리스트 조회 API",
            description = """
                    찜한 상시 정책 리스트를 조회하는 API입니다. \n
                    사용자가 최근 찜한 순입니다.

                    ### Query Parameter
                    - `cursor`: 최초 요청에서는 생략하고, 다음 요청부터 응답의 `nextCursor`를 전달합니다.
                    - `size`: 한 번에 조회할 정책 수(기본 20, 최대 50)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "찜한 상시 정책 리스트 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 커서"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    CommonResponse<PolicyListResponse> getOpenEndedLikedPolicies(
            @CurrentUser User user,
            @Parameter(description = "이전 응답에서 받은 다음 페이지 커서")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "조회 개수", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    );

    @Operation(
            summary = "내 정책 전체보기 API",
            description = """
                    사용자가 찜한 정책을 카테고리별로 조회할 수 있는 API입니다. \n

                    ### Query Parameter
                    - `category`: 정책 분야
                    - `sort`: `LATEST`(최신순), `DEADLINE`(마감순)
                    - `cursor`: 최초 요청에서는 생략하고, 다음 요청부터 응답의 `nextCursor`를 전달합니다.
                    - `size`: 한 번에 조회할 정책 수(기본 20, 최대 50)
                    """)
    CommonResponse<PolicyListResponse> getMyPoliciesByCategory(
            @CurrentUser User user,
            @Parameter(description = "정책 분야", example = "JOB_STARTUP")
            @RequestParam(required = false) PolicyCategoryType category,
            @Parameter(description = "정렬 기준", example = "LATEST")
            @RequestParam(defaultValue = "LATEST") PolicyListSortType sort,
            @Parameter(description = "이전 응답에서 받은 다음 페이지 커서")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "조회 개수", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    );

    @Operation(
            summary = "캘린더 조회 API",
            description = """
                    사용자가 찜한 정책 중 마감일이 있는 정책을 캘린더 바 형태로 표시하기 위한 API입니다. \n

                    ### Query Parameter
                    - `targetMonth`: 사용자가 선택한 연도와 월로, YYYY-MM 형식입니다.
                    
                    ### Response
                    마감일인 날짜들을 리스트로 묶어, 응답으로 반환합니다.
                    
                    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "캘린더 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 연월 형식"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    CommonResponse<CalendarResponse> getMyCalendar(
            @CurrentUser User user,
            @Parameter(description = "사용자가 확인하고 싶은 연도와 월", example = "2026-08")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth targetMonth
    );

    @Operation(
            summary = "정책 메모 조회 API",
            description = """
                    사용자가 찜한 정책에 작성한 메모를 조회하는 API입니다. \n
                    정책 카테고리, 마감 표시, 정책 제목과 함께 메모 내용을 반환합니다.
                    찜한 정책이지만 아직 작성한 메모가 없으면 빈 문자열을 반환합니다.

                    ### Path Variables
                    - `policyId`: 정책 id
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정책 메모 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "찜한 정책을 찾을 수 없음")
    })
    CommonResponse<PolicyMemoResponse> getPolicyMemo(
            @CurrentUser User user,
            @Parameter(description = "정책 id", example = "1", required = true)
            @PathVariable Long policyId
    );

    @Operation(
            summary = "정책 메모 작성 및 수정 API",
            description = """
                    사용자가 찜한 정책에 메모를 작성하거나 수정하는 API입니다. \n
                    메모는 찜한 정책에만 작성할 수 있으며, 같은 API로 기존 메모 내용을 수정합니다.

                    ### Path Variables
                    - `policyId`: 정책 id

                    ### Request Body
                    - `memo`: 사용자가 정책과 관련하여 작성한 메모 내용
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정책 메모 작성 및 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 메모 내용"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "찜한 정책을 찾을 수 없음")
    })
    CommonResponse<String> updatePolicyMemo(
            @CurrentUser User user,
            @Parameter(description = "정책 id", example = "1", required = true)
            @PathVariable Long policyId,
            @Valid @RequestBody PolicyMemoRequest request
    );
}
