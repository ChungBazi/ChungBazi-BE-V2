package com.chungbazi.server.domain.notification.application.mapper;

import com.chungbazi.server.domain.notification.application.dto.NotificationPushMessage;
import com.chungbazi.server.domain.notification.application.dto.PolicyReminderTargets;
import com.chungbazi.server.domain.notification.domain.Notification;
import com.chungbazi.server.domain.notification.domain.type.NotificationCategory;
import com.chungbazi.server.domain.notification.domain.type.NotificationType;
import com.chungbazi.server.domain.policy.domain.entity.PolicyLike;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class NotificationReminderMapper {

    private static final String PREPARATION_TITLE = "찜한 정책, 미리 준비해볼까요?";
    private static final String PREPARATION_MESSAGE_BODY =
            "신청할 때 놓치는 내용이 없도록 필요한 정보와 신청 방법을 미리 살펴보세요.";
    private static final String DEADLINE_D7_TITLE = "찜한 정책 신청이 일주일 남았어요!";
    private static final String DEADLINE_D7_MESSAGE_BODY =
            "여유 있게 준비할 수 있도록 신청 방법과 필요한 정보를 미리 확인해보세요.";
    private static final String DEADLINE_D3_TITLE = "찜한 정책 신청이 3일 남았어요!";
    private static final String DEADLINE_D3_MESSAGE_BODY =
            "신청 기간을 놓치지 않도록 필요한 정보를 확인하고 신청을 준비해보세요.";
    private static final String SINGLE_POLICY_PUSH_TITLE_FORMAT =
            "\"%s\" 정책을 신청해보세요.";
    private static final String MULTIPLE_POLICIES_PUSH_TITLE_FORMAT =
            "\"%s\" 외 찜한 정책을 신청해보세요.";
    private static final String MULTIPLE_PREPARATION_PUSH_MESSAGE =
            "신청에 필요한 정보와 신청 방법을 미리 확인해보세요.";
    private static final String MULTIPLE_DEADLINE_PUSH_MESSAGE =
            "신청 마감이 다가온 찜한 정책들을 확인해보세요.";

    public Map<Long, NotificationType> toReminderTypeByPolicyId(PolicyReminderTargets targets) {
        Map<Long, NotificationType> reminderTypeByPolicyId = new HashMap<>();
        targets.deadlineInSevenDaysPolicies().forEach(policy ->
                reminderTypeByPolicyId.put(policy.getId(), NotificationType.POLICY_DEADLINE_D7)
        );
        targets.deadlineInThreeDaysPolicies().forEach(policy ->
                reminderTypeByPolicyId.put(policy.getId(), NotificationType.POLICY_DEADLINE_D3)
        );
        return reminderTypeByPolicyId;
    }

    public Notification toDeadlineNotification(PolicyLike policyLike, NotificationType type) {
        boolean deadlineInSevenDays = type == NotificationType.POLICY_DEADLINE_D7;
        return Notification.create(
                policyLike.getUserId(),
                NotificationCategory.MY_POLICY,
                type,
                deadlineInSevenDays ? DEADLINE_D7_TITLE : DEADLINE_D3_TITLE,
                messageWithPolicyTitle(
                        policyLike,
                        deadlineInSevenDays
                                ? DEADLINE_D7_MESSAGE_BODY
                                : DEADLINE_D3_MESSAGE_BODY
                ),
                policyLike.getPolicy().getId()
        );
    }

    public Notification toPreparationNotification(PolicyLike policyLike) {
        return Notification.create(
                policyLike.getUserId(),
                NotificationCategory.MY_POLICY,
                NotificationType.POLICY_PREPARATION,
                PREPARATION_TITLE,
                messageWithPolicyTitle(policyLike, PREPARATION_MESSAGE_BODY),
                policyLike.getPolicy().getId()
        );
    }

    public List<NotificationPushMessage> toRepresentativePushMessages(
            List<Notification> notifications,
            List<PolicyLike> policyLikes
    ) {
        Map<Long, String> policyTitleByPolicyId = policyLikes.stream()
                .collect(Collectors.toMap(
                        policyLike -> policyLike.getPolicy().getId(),
                        policyLike -> policyLike.getPolicy().getTitle(),
                        (existingTitle, ignored) -> existingTitle
                ));

        Map<Long, List<Notification>> notificationsByUserId = notifications.stream()
                .collect(Collectors.groupingBy(
                        Notification::getUserId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        return notificationsByUserId.values().stream()
                .map(userNotifications -> toRepresentativePushMessage(
                        userNotifications,
                        policyTitleByPolicyId
                ))
                .toList();
    }

    private NotificationPushMessage toRepresentativePushMessage(
            List<Notification> userNotifications,
            Map<Long, String> policyTitleByPolicyId
    ) {
        Notification representativeNotification = userNotifications.getFirst();
        String policyTitle = policyTitleByPolicyId.get(representativeNotification.getPolicyId());
        if (policyTitle == null) {
            throw new IllegalArgumentException(
                    "대표 알림에 해당하는 정책 제목이 없습니다. policyId="
                            + representativeNotification.getPolicyId()
            );
        }

        String pushTitle = userNotifications.size() == 1
                ? SINGLE_POLICY_PUSH_TITLE_FORMAT.formatted(policyTitle)
                : MULTIPLE_POLICIES_PUSH_TITLE_FORMAT.formatted(policyTitle);
        String pushMessage = toPushMessage(userNotifications, representativeNotification);
        return NotificationPushMessage.from(
                representativeNotification,
                pushTitle,
                pushMessage
        );
    }

    private String toPushMessage(
            List<Notification> userNotifications,
            Notification representativeNotification
    ) {
        if (userNotifications.size() == 1) {
            return representativeNotification.getMessage();
        }

        boolean preparationReminders = userNotifications.stream()
                .allMatch(notification ->
                        notification.getType() == NotificationType.POLICY_PREPARATION
                );
        return preparationReminders
                ? MULTIPLE_PREPARATION_PUSH_MESSAGE
                : MULTIPLE_DEADLINE_PUSH_MESSAGE;
    }

    private String messageWithPolicyTitle(PolicyLike policyLike, String messageBody) {
        return "찜한 정책 '%s': %s".formatted(
                policyLike.getPolicy().getTitle(),
                messageBody
        );
    }
}
