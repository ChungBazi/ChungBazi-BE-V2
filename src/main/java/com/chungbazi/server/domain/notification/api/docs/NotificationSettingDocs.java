package com.chungbazi.server.domain.notification.api.docs;

import com.chungbazi.server.domain.notification.api.dto.request.NotificationSettingUpdateRequest;
import com.chungbazi.server.domain.notification.api.dto.response.NotificationSettingResponse;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.resolver.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "[Notification Setting]", description = "알림 설정 관련 API")
public interface NotificationSettingDocs {

    @Operation(
            summary = "알림 설정 조회 API",
            description = "현재 사용자의 전체 알림, 내 정책 알림, 청바지 알림 수신 여부를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 설정 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    CommonResponse<NotificationSettingResponse> getNotificationSetting(
            @CurrentUser User user
    );

    @Operation(
            summary = "알림 설정 변경 API",
            description = """
                    전체 알림, 내 정책 알림, 청바지 알림 설정을 한 번에 변경합니다.

                    ### Request Body
                    - `allNotificationEnabled` : 전체 알림 수신 여부로, boolean 값입니다.
                    - `policyNotificationEnabled` : 내 정책 알림 수신 여부로, boolean 값입니다.
                    - `chungbaziNotificationEnabled` : 청바지 알림 수신 여부로, boolean 값입니다.

                    - 전체 알림이 `false`이면 하위 두 설정도 `false`로 저장됩니다.
                    - 전체 알림이 `true`이면 하위 두 설정은 요청받은 값을 각각 유지합니다.
                    - 전체 알림이 `false`인데 하위 알림이 하나라도 `true`이면 잘못된 요청입니다.
                    - 전체 알림이 `true`인데 하위 알림이 모두 `false`이면 잘못된 요청입니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "알림 설정 변경 성공"),
            @ApiResponse(responseCode = "400", description = "필수 설정값 누락"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    CommonResponse<NotificationSettingResponse> updateNotificationSetting(
            @CurrentUser User user,
            @Valid @RequestBody NotificationSettingUpdateRequest request
    );
}
