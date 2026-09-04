package com.chungbazi.server.domain.notification.application;

import com.chungbazi.server.domain.notification.application.dto.InterestPolicyNotificationTarget;
import com.chungbazi.server.domain.notification.application.dto.NotificationKey;
import com.chungbazi.server.domain.notification.application.event.InterestPolicyNotificationsCreatedEvent;
import com.chungbazi.server.domain.notification.application.mapper.InterestPolicyNotificationMapper;
import com.chungbazi.server.domain.notification.domain.Notification;
import com.chungbazi.server.domain.notification.domain.repository.NotificationRepository;
import com.chungbazi.server.domain.notification.domain.type.NotificationType;
import com.chungbazi.server.domain.policy.application.event.NewPoliciesRegisteredEvent;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.UserInterest;
import com.chungbazi.server.domain.user.infrastructure.UserInterestRepository;
import com.chungbazi.server.domain.user.infrastructure.UserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterestPolicyNotificationService {

    private static final int USER_CHUNK_SIZE = 1_000;
    private static final long INITIAL_USER_CURSOR = 0L;

    private final PolicyRepository policyRepository;
    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final NotificationRepository notificationRepository;
    private final InterestPolicyNotificationMapper interestPolicyNotificationMapper;
    private final ApplicationEventPublisher eventPublisher;

    public void createInterestPolicyNotifications(NewPoliciesRegisteredEvent event) {
        List<Policy> newPolicies = policyRepository.findVisiblePoliciesByIdIn(
                event.policyIds(),
                RecruitmentStatus.CLOSED
        );
        if (newPolicies.isEmpty()) {
            return;
        }

        Set<PolicySubCategoryType> subCategories = newPolicies.stream()
                .map(Policy::getSubCategory)
                .collect(java.util.stream.Collectors.toSet());
        long cursor = INITIAL_USER_CURSOR;

        while (true) {
            List<User> users = userRepository.findInterestNotificationTargetUsersAfterId(
                    subCategories,
                    cursor,
                    PageRequest.of(0, USER_CHUNK_SIZE)
            );
            if (users.isEmpty()) {
                return;
            }

            Set<Long> userIds = users.stream()
                    .map(User::getId)
                    .collect(java.util.stream.Collectors.toSet());

            List<UserInterest> interests =
                    userInterestRepository.findAllByUserIdsAndSubCategories(
                            userIds,
                            subCategories
                    );
            List<InterestPolicyNotificationTarget> targets = findInterestPolicyTargets(
                    users,
                    interests,
                    newPolicies
            );

            log.info(
                    "관심 분야 신규 정책 알림 대상 선별 완료. userCount={}, targetUserCount={}",
                    users.size(),
                    targets.size()
            );

            List<Notification> notifications = saveInterestPolicyNotifications(targets);
            log.info(
                    "관심 분야 신규 정책 알림 저장 완료. notificationCount={}",
                    notifications.size()
            );

            // FCM 발송
            if (!notifications.isEmpty()) {
                eventPublisher.publishEvent(InterestPolicyNotificationsCreatedEvent.of(
                        //알림이 여러 개일 경우 한 개만 FCM 발송
                        interestPolicyNotificationMapper.toRepresentativePushMessages(
                                notifications
                        )
                ));
            }

            cursor = users.getLast().getId();
        }
    }

    public List<Notification> saveInterestPolicyNotifications(
            List<InterestPolicyNotificationTarget> targets
    ) {
        if (targets.isEmpty()) {
            return List.of();
        }

        Set<Long> userIds = targets.stream()
                .map(target -> target.user().getId())
                .collect(java.util.stream.Collectors.toSet());
        Set<Long> policyIds = targets.stream()
                .flatMap(target -> target.policies().stream())
                .map(Policy::getId)
                .collect(java.util.stream.Collectors.toSet());

        Set<NotificationKey> existingNotificationKeys = notificationRepository
                .findAllByUserIdInAndPolicyIdInAndTypeIn(
                        userIds,
                        policyIds,
                        Set.of(
                                NotificationType.PERSONALIZED_POLICY,
                                NotificationType.INTEREST_POLICY
                        )
                ).stream()
                .map(NotificationKey::from)
                .collect(java.util.stream.Collectors.toSet());

        List<Notification> notifications = targets.stream()
                .flatMap(target -> target.policies().stream()
                        .map(policy -> interestPolicyNotificationMapper.toNotification(
                                target.user(),
                                policy
                        )))
                .filter(notification -> !hasExistingNotification(
                        notification,
                        existingNotificationKeys
                ))
                .toList();

        if (notifications.isEmpty()) {
            return List.of();
        }
        return notificationRepository.saveAll(notifications);
    }

    public List<InterestPolicyNotificationTarget> findInterestPolicyTargets(
            List<User> users,
            List<UserInterest> interests,
            List<Policy> newPolicies
    ) {
        Map<Long, Set<PolicySubCategoryType>> subCategoriesByUserId = new HashMap<>();
        interests.forEach(interest -> subCategoriesByUserId
                .computeIfAbsent(interest.getUser().getId(), key -> new HashSet<>())
                .add(interest.getSubCategory()));

        List<InterestPolicyNotificationTarget> targets = new ArrayList<>();
        for (User user : users) {
            Set<PolicySubCategoryType> userSubCategories =
                    subCategoriesByUserId.getOrDefault(user.getId(), Set.of());
            List<Policy> matchedPolicies = newPolicies.stream()
                    .filter(policy -> userSubCategories.contains(policy.getSubCategory()))
                    .toList();
            if (!matchedPolicies.isEmpty()) {
                targets.add(InterestPolicyNotificationTarget.of(user, matchedPolicies));
            }
        }
        return targets;
    }

    private boolean hasExistingNotification(
            Notification notification,
            Set<NotificationKey> existingNotificationKeys
    ) {
        NotificationKey interestPolicyKey = NotificationKey.from(notification);
        NotificationKey personalizedPolicyKey = new NotificationKey(
                notification.getUserId(),
                notification.getPolicyId(),
                NotificationType.PERSONALIZED_POLICY
        );
        return existingNotificationKeys.contains(interestPolicyKey)
                || existingNotificationKeys.contains(personalizedPolicyKey);
    }
}
