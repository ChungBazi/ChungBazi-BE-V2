package com.chungbazi.server.domain.notification.api.dto.response;

import com.chungbazi.server.domain.notification.application.mapper.NotificationElapsedTimeFormatter;
import com.chungbazi.server.domain.notification.domain.Notification;
import com.chungbazi.server.domain.notification.domain.type.NotificationCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "알림 목록 무한스크롤 응답")
public record NotificationListResponse(
        @Schema(description = "조회된 알림 목록")
        List<NotificationItem> notifications,

        @Schema(description = "다음 페이지 조회에 사용할 알림 ID. 다음 페이지가 없으면 null", example = "42", nullable = true)
        Long nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {
    public static NotificationListResponse of(
            List<Notification> notifications,
            Long nextCursor,
            boolean hasNext,
            LocalDateTime now
    ) {
        return new NotificationListResponse(
                notifications.stream()
                        .map(notification -> NotificationItem.from(notification, now))
                        .toList(),
                nextCursor,
                hasNext
        );
    }

    @Schema(description = "알림 목록 항목")
    public record NotificationItem(
            @Schema(description = "알림 ID", example = "43")
            Long notificationId,

            @Schema(description = "알림 카테고리", example = "MY_POLICY")
            NotificationCategory category,

            @Schema(description = "알림 제목", example = "찜한 정책 신청 마감이 하루 남았어요")
            String title,

            @Schema(description = "알림 내용", example = "찜한 정책의 신청 기간을 확인해보세요.")
            String message,

            @Schema(description = "연결된 정책 ID. 단일 정책과 연결되지 않은 알림이면 null", example = "15", nullable = true)
            Long policyId,

            @Schema(description = "알림이 생성된 후 지난 시간", example = "17분 전")
            String elapsedTime
    ) {
        public static NotificationItem from(Notification notification, LocalDateTime now) {
            return new NotificationItem(
                    notification.getId(),
                    notification.getCategory(),
                    notification.getTitle(),
                    notification.getMessage(),
                    notification.getPolicyId(),
                    NotificationElapsedTimeFormatter.format(notification.getCreatedAt(), now)
            );
        }
    }
}
