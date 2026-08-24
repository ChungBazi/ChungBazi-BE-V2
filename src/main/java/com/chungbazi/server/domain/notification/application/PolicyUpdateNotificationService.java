package com.chungbazi.server.domain.notification.application;

import com.chungbazi.server.domain.notification.application.event.PolicyUpdateNotificationsCreatedEvent;
import com.chungbazi.server.domain.notification.application.mapper.PolicyUpdateNotificationMapper;
import com.chungbazi.server.domain.notification.domain.Notification;
import com.chungbazi.server.domain.notification.domain.repository.NotificationRepository;
import com.chungbazi.server.domain.notification.domain.type.NotificationType;
import com.chungbazi.server.domain.policy.application.event.PolicyInformationChangedEvent;
import com.chungbazi.server.domain.policy.domain.entity.PolicyLike;
import com.chungbazi.server.domain.policy.domain.repository.PolicyLikeRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyUpdateNotificationService {

    private final PolicyLikeRepository policyLikeRepository;
    private final NotificationRepository notificationRepository;
    private final PolicyUpdateNotificationMapper policyUpdateNotificationMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createPolicyUpdateNotifications(PolicyInformationChangedEvent event) {
        List<PolicyLike> recipients =
                policyLikeRepository.findNotificationRecipientsByPolicyIds(
                        Set.of(event.policyId())
                );

        log.info(
                "찜한 정책 정보 변경 알림 대상 조회 완료. policyId={}, sourceModifiedAt={}, recipientCount={}",
                event.policyId(),
                event.sourceModifiedAt(),
                recipients.size()
        );

        if (recipients.isEmpty()) {
            return;
        }

        Set<Long> recipientUserIds = recipients.stream()
                .map(PolicyLike::getUserId)
                .collect(java.util.stream.Collectors.toSet());
        Set<Long> alreadyNotifiedUserIds = new HashSet<>(
                notificationRepository
                        .findAllByUserIdInAndPolicyIdAndTypeAndPolicySourceModifiedAt(
                                recipientUserIds,
                                event.policyId(),
                                NotificationType.POLICY_UPDATED,
                                event.sourceModifiedAt()
                        ).stream()
                        .map(Notification::getUserId)
                        .toList()
        );

        List<Notification> notifications = recipients.stream()
                .filter(policyLike -> !alreadyNotifiedUserIds.contains(policyLike.getUserId()))
                .map(policyLike -> policyUpdateNotificationMapper.toNotification(
                        policyLike,
                        event.sourceModifiedAt()
                ))
                .toList();

        if (notifications.isEmpty()) {
            return;
        }

        notificationRepository.saveAll(notifications);

        //FCM 발송
        eventPublisher.publishEvent(PolicyUpdateNotificationsCreatedEvent.of(
                policyUpdateNotificationMapper.toPushMessages(notifications)
        ));
    }
}
