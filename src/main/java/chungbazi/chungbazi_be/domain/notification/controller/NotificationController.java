package chungbazi.chungbazi_be.domain.notification.controller;

import chungbazi.chungbazi_be.domain.notification.dto.request.FcmTokenRequestDTO;
import chungbazi.chungbazi_be.domain.notification.dto.response.NotificationResponseDTO;
import chungbazi.chungbazi_be.domain.notification.dto.request.NotificationSettingRequestDTO;
import chungbazi.chungbazi_be.domain.notification.dto.response.NotificationSettingResponseDTO;
import chungbazi.chungbazi_be.domain.notification.entity.enums.NotificationType;
import chungbazi.chungbazi_be.domain.notification.service.FcmTokenService;
import chungbazi.chungbazi_be.domain.notification.service.NotificationService;
import chungbazi.chungbazi_be.domain.notification.service.NotificationSettingService;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.support.UserHelper;
import chungbazi.chungbazi_be.global.apiPayload.ApiResponse;
import chungbazi.chungbazi_be.global.utils.PaginationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "[알림]", description = "알림 및 수신 설정 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final NotificationSettingService notificationSettingService;
    private final FcmTokenService fcmTokenService;
    private final UserHelper userHelper;

    @PostMapping()
    @Operation(summary = "FCM 토큰 저장 API", description = "FCM 토큰을 저장하는 API입니다.")
    public ApiResponse<String> saveFcmToken(@RequestBody @Valid FcmTokenRequestDTO requestDTO) {
        User user = userHelper.getAuthenticatedUser();
        fcmTokenService.registerOrUpdateToken(user, requestDTO.fcmToken());

        return ApiResponse.onSuccess("FCM 토큰 저장이 완료되었습니다.");
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "특정 알림 읽음 상태 변경 API")
    public ApiResponse<String> readNotification(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ApiResponse.onSuccess("알림 읽음 상태가 변경되었습니다.");
    }

    @GetMapping
    @Operation(summary = "알림 조회 API",description = "알림을 조회하는 API입니다. 전체 알림을 조회하고 싶으면 type 입력을 안하시면 됩니다.")
    public ApiResponse<PaginationResult<NotificationResponseDTO.notificationsDto>> getNotifications(
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "15") int limit){

        PaginationResult<NotificationResponseDTO.notificationsDto> response=notificationService.getNotifications(type,cursor,limit);

        return ApiResponse.onSuccess(response);
    }

    @PatchMapping("/settings-up")
    @Operation(summary = "알림 수신 설정 API",description = """
            알림 수신을 설정하는 api입니다.
            * policy_alarm은 캘린더 정책 알림 수신 설정,
            * community_alarm은 커뮤니티 관련 알림 수신 설정,
            * reward_alarm은 리워드 알림 수신 설정,
            * notice_alarm은 공지사항 알림 수신 설정으로,
            알림 끄기를 원한다면 false를, 알림 켜기를 원한다면 true를 입력해주시면 됩니다.
            """)
    public ApiResponse<NotificationSettingResponseDTO.settingResDto> updateNotificationSetting(
            @RequestBody NotificationSettingRequestDTO request
    ) {
        NotificationSettingResponseDTO.settingResDto response=notificationSettingService.setNotificationSetting(request);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/settings")
    @Operation(summary = "알림 수신 설정 조회 API",description = "현재 유저의 알림 수신 설정을 조회하는 API입니다")
    public ApiResponse<NotificationSettingResponseDTO.settingResDto> getNotificationSetting() {
        NotificationSettingResponseDTO.settingResDto response=notificationSettingService.getNotificationSetting();
        return ApiResponse.onSuccess(response);
    }

}
