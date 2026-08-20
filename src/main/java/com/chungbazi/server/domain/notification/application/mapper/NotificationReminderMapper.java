package com.chungbazi.server.domain.notification.application.mapper;

import com.chungbazi.server.domain.notification.application.dto.NotificationPushMessage;
import com.chungbazi.server.domain.notification.application.dto.PolicyReminderTargets;
import com.chungbazi.server.domain.notification.domain.Notification;
import com.chungbazi.server.domain.notification.domain.type.NotificationCategory;
import com.chungbazi.server.domain.notification.domain.type.NotificationType;
import com.chungbazi.server.domain.policy.domain.entity.PolicyLike;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
// TODO:정책 제목으로 부연설명 추가
public class NotificationReminderMapper {

    private static final String PREPARATION_TITLE = "찜한 정책, 미리 준비해볼까요?";
    private static final String PREPARATION_MESSAGE =
            "신청할 때 놓치는 내용이 없도록 필요한 정보와 신청 방법을 미리 살펴보세요.";
    private static final String DEADLINE_D7_TITLE = "찜한 정책 신청이 일주일 남았어요!";
    private static final String DEADLINE_D7_MESSAGE =
            "여유 있게 준비할 수 있도록 신청 방법과 필요한 정보를 미리 확인해보세요.";
    private static final String DEADLINE_D3_TITLE = "찜한 정책 신청이 3일 남았어요!";
    private static final String DEADLINE_D3_MESSAGE =
            "신청 기간을 놓치지 않도록 필요한 정보를 확인하고 신청을 준비해보세요.";

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
                deadlineInSevenDays ? DEADLINE_D7_MESSAGE : DEADLINE_D3_MESSAGE,
                policyLike.getPolicy().getId()
        );
    }

    public Notification toPreparationNotification(PolicyLike policyLike) {
        return Notification.create(
                policyLike.getUserId(),
                NotificationCategory.MY_POLICY,
                NotificationType.POLICY_PREPARATION,
                PREPARATION_TITLE,
                PREPARATION_MESSAGE,
                policyLike.getPolicy().getId()
        );
    }

    public List<NotificationPushMessage> toPushMessages(List<Notification> notifications) {
        return notifications.stream()
                .map(NotificationPushMessage::from)
                .toList();
    }
}
