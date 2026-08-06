package com.chungbazi.server.domain.policy.api.docs;

import com.chungbazi.server.domain.policy.api.dto.response.MyPolicyDeadlineResponse;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse;
import com.chungbazi.server.domain.policy.domain.type.PolicyListSortType;
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
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
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

                    ### Query Parameter
                    - `targetDate`: 사용자가 확인하고 싶은 날짜로, YYYY-MM-DD 형식입니다.
                    - `sort`: `LATEST`(최신순), `DEADLINE`(마감순)
                    - `cursor`: 최초 요청에서는 생략하고, 다음 요청부터 응답의 `nextCursor`를 전달합니다.
                    - `size`: 한 번에 조회할 정책 수(기본 20, 최대 50)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "현재 날짜에 마감되는 찜한 정책 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 날짜, 정렬 또는 커서"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    CommonResponse<PolicyListResponse> getDeadlinePoliciesByDate(
            @CurrentUser User user,
            @Parameter(description = "사용자가 확인하고 싶은 날짜", example = "2026-08-06", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate,
            @Parameter(description = "정렬 기준", example = "LATEST")
            @RequestParam(defaultValue = "LATEST") PolicyListSortType sort,
            @Parameter(description = "이전 응답에서 받은 다음 페이지 커서")
            @RequestParam(required = false) String cursor,
            @Parameter(description = "조회 개수", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    );
}
