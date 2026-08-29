package com.chungbazi.server.domain.notification.infrastructure.scheduler;

import com.chungbazi.server.domain.notification.application.NotificationReminderService;
import com.chungbazi.server.domain.notification.application.dto.DeadlineReminderCreationResult;
import com.chungbazi.server.domain.notification.application.dto.PreparationReminderCreationResult;
import com.chungbazi.server.global.logging.ScheduledWorkflowMdc;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationReminderScheduler {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final NotificationReminderService notificationReminderService;

    @Scheduled(
            cron = "${notification.reminder.cron:0 0 9 * * *}",
            zone = "Asia/Seoul"
    )
    public void sendPolicyReminderNotifications() {
        try (ScheduledWorkflowMdc ignored = ScheduledWorkflowMdc.start("deadline-reminder")) {
            try {
                log.info("정책 리마인드 알림 스케줄러 실행");

                DeadlineReminderCreationResult result =
                        notificationReminderService.createDeadlineReminderNotifications(
                                LocalDate.now(SERVICE_ZONE_ID)
                        );

                log.info(
                        "정책 리마인드 알림 생성 완료. 마감 7일 전 정책 수={}, 마감 3일 전 정책 수={}, 생성 알림 수={}",
                        result.deadlineInSevenDaysPolicyCount(),
                        result.deadlineInThreeDaysPolicyCount(),
                        result.createdNotificationCount()
                );
            } catch (RuntimeException exception) {
                log.error("정책 리마인드 알림 스케줄러 실패", exception);
                throw exception;
            }
        }
    }

    @Scheduled(
            cron = "${notification.reminder.preparation-cron:0 0 * * * *}",
            zone = "Asia/Seoul"
    )
    public void sendPolicyPreparationNotifications() {
        try (ScheduledWorkflowMdc ignored = ScheduledWorkflowMdc.start("preparation-reminder")) {
            try {
                log.info("찜한 정책 신청 준비 알림 스케줄러 실행");

                PreparationReminderCreationResult result =
                        notificationReminderService.createPreparationReminderNotifications(
                                LocalDateTime.now(SERVICE_ZONE_ID)
                        );

                log.info(
                        "찜한 정책 신청 준비 알림 생성 완료. 대상 수={}, 생성 알림 수={}",
                        result.targetCount(),
                        result.createdNotificationCount()
                );
            } catch (RuntimeException exception) {
                log.error("찜한 정책 신청 준비 알림 스케줄러 실패", exception);
                throw exception;
            }
        }
    }
}
