package com.chungbazi.server.domain.notification.api.docs;

import com.chungbazi.server.domain.notification.api.dto.response.NotificationListResponse;
import com.chungbazi.server.domain.notification.domain.type.NotificationCategory;
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
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "[Notification]", description = "알림 관련 API")
public interface NotificationDocs {

    @Operation(
            summary = "알림 목록 조회 API",
            description = """
                    현재 사용자의 알림을 `notificationId` 내림차순으로 조회합니다.

                    ### Query Parameter
                    - `category`: 알림 카테고리. 생략하면 전체 알림 조회
                      - `MY_POLICY`: 내 정책 알림
                      - `CHUNGBAZI`: 청바지 알림
                    - `cursor`: 이전 응답의 `nextCursor`. 최초 요청에서는 생략
                    - `size`: 한 번에 조회할 알림 수. 기본 20, 최대 50

                    ### ResponseBody
                    - `notifications`: 알림 목록
                    - `notifications[].elapsedTime`: `n분 전`, `n시간 전`, `n일 전` 형식의 경과 시간
                    - `nextCursor`: 다음 페이지 조회에 사용할 마지막 알림 ID
                    - `hasNext`: 다음 페이지 존재 여부
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 카테고리, 커서 또는 조회 개수"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    CommonResponse<NotificationListResponse> getNotifications(
            @CurrentUser User user,
            @Parameter(description = "알림 카테고리. 생략하면 전체 조회", example = "MY_POLICY")
            @RequestParam(required = false) NotificationCategory category,
            @Parameter(description = "이전 응답에서 받은 마지막 알림 ID", example = "42")
            @RequestParam(required = false) @Min(1) Long cursor,
            @Parameter(description = "조회 개수", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    );

    @Operation(
            summary = "알림 읽음 처리 API",
            description = "현재 사용자의 알림 한 건을 읽음 상태로 변경합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
    })
    CommonResponse<String> markNotificationAsRead(
            @CurrentUser User user,
            @Parameter(description = "읽음 처리할 알림 ID", example = "43", required = true)
            @PathVariable Long notificationId
    );
}
