package com.chungbazi.server.domain.notification.application;

import com.chungbazi.server.domain.notification.application.dto.NotificationKey;
import com.chungbazi.server.domain.notification.application.dto.PersonalizedPolicyNotificationTarget;
import com.chungbazi.server.domain.notification.application.event.PersonalizedPolicyNotificationsCreatedEvent;
import com.chungbazi.server.domain.notification.application.mapper.PersonalizedPolicyContextMapper;
import com.chungbazi.server.domain.notification.application.mapper.PersonalizedPolicyNotificationMapper;
import com.chungbazi.server.domain.notification.domain.Notification;
import com.chungbazi.server.domain.notification.domain.repository.NotificationRepository;
import com.chungbazi.server.domain.notification.domain.type.NotificationType;
import com.chungbazi.server.domain.policy.application.dto.PolicyRecommendationContext;
import com.chungbazi.server.domain.policy.application.event.NewPoliciesRegisteredEvent;
import com.chungbazi.server.domain.policy.application.support.PersonalizedPolicyRanker;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.PolicyRegion;
import com.chungbazi.server.domain.policy.domain.repository.PolicyRegionRepository;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.UserInterest;
import com.chungbazi.server.domain.user.infrastructure.UserInterestRepository;
import com.chungbazi.server.domain.user.infrastructure.UserRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
public class PersonalizedPolicyNotificationService {

    private static final int USER_CHUNK_SIZE = 1_000;
    private static final int PERSONALIZED_POLICY_SIZE = 5;
    private static final long INITIAL_USER_CURSOR = 0L;

    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final PolicyRepository policyRepository;
    private final PolicyRegionRepository policyRegionRepository;
    private final NotificationRepository notificationRepository;
    private final PersonalizedPolicyContextMapper personalizedPolicyContextMapper;
    private final PersonalizedPolicyNotificationMapper personalizedPolicyNotificationMapper;
    private final PersonalizedPolicyRanker personalizedPolicyRanker;
    private final ApplicationEventPublisher eventPublisher;

    public void createPersonalizedPolicyNotifications(NewPoliciesRegisteredEvent event) {
        List<Policy> newPolicies = policyRepository.findVisiblePoliciesByIdIn(
                event.policyIds(),
                RecruitmentStatus.CLOSED
        );
        if (newPolicies.isEmpty()) {
            return;
        }
        Map<Long, List<PolicyRegion>> policyRegionsByPolicyId = findPolicyRegionsByPolicyId(
                newPolicies.stream().map(Policy::getId).toList()
        );

        long cursor = INITIAL_USER_CURSOR;
        int chunkNumber = 0;

        //사용자 배치 조회
        while (true) {
            List<User> users = userRepository.findNotificationTargetUsersAfterId(
                    cursor,
                    PageRequest.of(0, USER_CHUNK_SIZE)
            );
            if (users.isEmpty()) {
                return;
            }

            chunkNumber++;

            Map<Long, PolicyRecommendationContext> contexts = findRecommendationContexts(users);

            //신규 맞춤 정책 알림 대상자 선별
            List<PersonalizedPolicyNotificationTarget> targets = findPersonalizedTargets(
                    users,
                    contexts,
                    newPolicies,
                    policyRegionsByPolicyId
            );

            //알림 저장
            List<Notification> notifications = savePersonalizedPolicyNotifications(targets);
            log.info(
                    "신규 맞춤 정책 알림 저장 완료. chunk={}, notificationCount={}",
                    chunkNumber,
                    notifications.size()
            );

            //FCM 알림 발송
            if (!notifications.isEmpty()) {
                eventPublisher.publishEvent(PersonalizedPolicyNotificationsCreatedEvent.of(
                        //알림이 여러 개일 경우 한 개만 FCM 발송
                        personalizedPolicyNotificationMapper.toRepresentativePushMessages(
                                notifications
                        )
                ));
            }

            cursor = users.getLast().getId();
        }
    }

    public List<Notification> savePersonalizedPolicyNotifications(
            List<PersonalizedPolicyNotificationTarget> targets
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
                        Set.of(NotificationType.PERSONALIZED_POLICY)
                ).stream()
                .map(NotificationKey::from)
                .collect(java.util.stream.Collectors.toSet());

        List<Notification> notifications = targets.stream()
                .flatMap(target -> target.policies().stream()
                        .map(policy -> personalizedPolicyNotificationMapper.toNotification(
                                target.user(),
                                policy
                        )))
                .filter(notification -> !existingNotificationKeys.contains(
                        NotificationKey.from(notification)
                ))
                .toList();

        return notificationRepository.saveAll(notifications);
    }

    public Map<Long, PolicyRecommendationContext> findRecommendationContexts(List<User> users) {
        Set<Long> userIds = users.stream()
                .map(User::getId)
                .collect(java.util.stream.Collectors.toSet());

        List<UserInterest> interests = userInterestRepository.findAllByUserIds(userIds);
        return personalizedPolicyContextMapper.toContexts(users, interests);
    }

    public List<PersonalizedPolicyNotificationTarget> findPersonalizedTargets(
            List<User> users,
            Map<Long, PolicyRecommendationContext> contexts,
            List<Policy> newPolicies,
            Map<Long, List<PolicyRegion>> policyRegionsByPolicyId
    ) {
        List<PersonalizedPolicyNotificationTarget> targets = new ArrayList<>();
        for (User user : users) {
            //유저 온보딩 정보 조회
            PolicyRecommendationContext context = contexts.get(user.getId());
            if (context == null) {
                continue;
            }

            List<Policy> regionalCandidates = newPolicies.stream()
                    .filter(policy -> isAvailableInUserRegion(
                            user,
                            policy,
                            policyRegionsByPolicyId.getOrDefault(policy.getId(), List.of())
                    ))
                    .toList();
            List<Policy> recommendedPolicies = personalizedPolicyRanker.rank(
                    user,
                    context,
                    regionalCandidates,
                    PERSONALIZED_POLICY_SIZE
            );
            if (!recommendedPolicies.isEmpty()) {
                targets.add(PersonalizedPolicyNotificationTarget.of(
                        user,
                        recommendedPolicies
                ));
            }
        }
        return targets;
    }

    private Map<Long, List<PolicyRegion>> findPolicyRegionsByPolicyId(
            Collection<Long> policyIds
    ) {
        Map<Long, List<PolicyRegion>> regionsByPolicyId = new HashMap<>();
        policyRegionRepository.findAllByPolicyIds(policyIds).forEach(policyRegion ->
                regionsByPolicyId
                        .computeIfAbsent(
                                policyRegion.getPolicy().getId(),
                                key -> new ArrayList<>()
                        )
                        .add(policyRegion)
        );
        return regionsByPolicyId;
    }

    private boolean isAvailableInUserRegion(
            User user,
            Policy policy,
            List<PolicyRegion> policyRegions
    ) {
        if (policy.isNational()) {
            return true;
        }
        if (user.getSidoCode() == null) {
            return false;
        }
        return policyRegions.stream().anyMatch(policyRegion ->
                policyRegion.getSidoCode() == user.getSidoCode()
                        && (
                                policyRegion.getRegionCode() == null
                                        || policyRegion.getRegionCode().getSigunguCode()
                                        .equals(user.getSigunguCode())
                        )
        );
    }
}
