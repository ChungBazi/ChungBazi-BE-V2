package com.chungbazi.server.domain.notification.application;

import com.chungbazi.server.domain.notification.application.dto.DeadlineReminderCreationResult;
import com.chungbazi.server.domain.notification.application.dto.NotificationKey;
import com.chungbazi.server.domain.notification.application.dto.PolicyReminderTargets;
import com.chungbazi.server.domain.notification.application.dto.PreparationReminderCreationResult;
import com.chungbazi.server.domain.notification.application.event.PolicyReminderNotificationsCreatedEvent;
import com.chungbazi.server.domain.notification.application.mapper.NotificationReminderMapper;
import com.chungbazi.server.domain.notification.domain.Notification;
import com.chungbazi.server.domain.notification.domain.repository.NotificationRepository;
import com.chungbazi.server.domain.notification.domain.type.NotificationType;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.PolicyLike;
import com.chungbazi.server.domain.policy.domain.repository.PolicyLikeRepository;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationReminderService {

    private final PolicyRepository policyRepository;
    private final PolicyLikeRepository policyLikeRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationReminderMapper notificationReminderMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public DeadlineReminderCreationResult createDeadlineReminderNotifications(LocalDate today) {
        PolicyReminderTargets targets = findReminderTargetPolicies(today);
        Map<Long, NotificationType> reminderTypeByPolicyId =
                notificationReminderMapper.toReminderTypeByPolicyId(targets);

        if (reminderTypeByPolicyId.isEmpty()) {
            return creationResult(targets, 0);
        }

        List<PolicyLike> recipients = policyLikeRepository.findNotificationRecipientsByPolicyIds(
                reminderTypeByPolicyId.keySet()
        );

        if (recipients.isEmpty()) {
            return creationResult(targets, 0);
        }

        Set<NotificationKey> existingNotificationKeys = findExistingNotificationKeys(
                recipients,
                new HashSet<>(reminderTypeByPolicyId.values())
        );

        List<Notification> notifications = recipients.stream()
                .map(policyLike -> notificationReminderMapper.toDeadlineNotification(
                        policyLike,
                        reminderTypeByPolicyId.get(policyLike.getPolicy().getId())
                ))
                .filter(notification -> !existingNotificationKeys.contains(NotificationKey.from(notification)))
                .toList();

        notificationRepository.saveAll(notifications);
        if (!notifications.isEmpty()) {
            eventPublisher.publishEvent(PolicyReminderNotificationsCreatedEvent.of(
                    notificationReminderMapper.toPushMessages(notifications)
            ));
        }
        return creationResult(targets, notifications.size());
    }

    public PolicyReminderTargets findReminderTargetPolicies(LocalDate today) {
        LocalDate deadlineInSevenDays = today.plusDays(7);
        LocalDate deadlineInThreeDays = today.plusDays(3);

        List<Policy> reminderTargetPolicies =
                policyRepository.findAllByApplyEndDateInAndRecruitmentStatusNot(
                        List.of(deadlineInSevenDays, deadlineInThreeDays),
                        RecruitmentStatus.CLOSED
                );

        List<Policy> deadlineInSevenDaysPolicies = reminderTargetPolicies.stream()
                .filter(policy -> deadlineInSevenDays.equals(policy.getApplyEndDate()))
                .toList();
        List<Policy> deadlineInThreeDaysPolicies = reminderTargetPolicies.stream()
                .filter(policy -> deadlineInThreeDays.equals(policy.getApplyEndDate()))
                .toList();

        return PolicyReminderTargets.of(
                deadlineInSevenDaysPolicies,
                deadlineInThreeDaysPolicies
        );
    }

    public List<PolicyLike> findPreparationReminderTargets(LocalDateTime executionTime) {
        LocalDateTime currentHour = executionTime.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime likedUntil = currentHour.minusHours(24);
        LocalDateTime likedFrom = likedUntil.minusHours(1);

        return policyLikeRepository.findPreparationReminderTargets(
                        likedFrom,
                        likedUntil,
                        RecruitmentStatus.CLOSED
                ).stream()
                .filter(this::hasMoreThanSevenDaysUntilDeadlineAtLikedDate)
                .toList();
    }

    @Transactional
    public PreparationReminderCreationResult createPreparationReminderNotifications(
            LocalDateTime executionTime
    ) {
        List<PolicyLike> targets = findPreparationReminderTargets(executionTime);
        if (targets.isEmpty()) {
            return PreparationReminderCreationResult.of(0, 0);
        }

        Set<NotificationKey> existingNotificationKeys = findExistingNotificationKeys(
                targets,
                Set.of(NotificationType.POLICY_PREPARATION)
        );

        List<Notification> notifications = targets.stream()
                .map(notificationReminderMapper::toPreparationNotification)
                .filter(notification -> !existingNotificationKeys.contains(NotificationKey.from(notification)))
                .toList();

        notificationRepository.saveAll(notifications);

        if (!notifications.isEmpty()) {
            eventPublisher.publishEvent(PolicyReminderNotificationsCreatedEvent.of(
                    notificationReminderMapper.toPushMessages(notifications)
            ));
        }

        return PreparationReminderCreationResult.of(targets.size(), notifications.size());
    }

    private Set<NotificationKey> findExistingNotificationKeys(
            List<PolicyLike> recipients,
            Set<NotificationType> notificationTypes
    ) {
        Set<Long> userIds = new HashSet<>();
        Set<Long> policyIds = new HashSet<>();
        recipients.forEach(policyLike -> {
            userIds.add(policyLike.getUserId());
            policyIds.add(policyLike.getPolicy().getId());
        });

        return notificationRepository.findAllByUserIdInAndPolicyIdInAndTypeIn(
                        userIds,
                        policyIds,
                        notificationTypes
                ).stream()
                .map(NotificationKey::from)
                .collect(java.util.stream.Collectors.toSet());
    }

    private DeadlineReminderCreationResult creationResult(
            PolicyReminderTargets targets,
            int createdNotificationCount
    ) {
        return DeadlineReminderCreationResult.of(
                targets.deadlineInSevenDaysPolicies().size(),
                targets.deadlineInThreeDaysPolicies().size(),
                createdNotificationCount
        );
    }

    private boolean hasMoreThanSevenDaysUntilDeadlineAtLikedDate(PolicyLike policyLike) {
        LocalDate applyEndDate = policyLike.getPolicy().getApplyEndDate();
        if (applyEndDate == null) {
            return true;
        }

        LocalDate sevenDaysAfterLikedDate = policyLike.getCreatedAt().toLocalDate().plusDays(7);
        return applyEndDate.isAfter(sevenDaysAfterLikedDate);
    }

}
