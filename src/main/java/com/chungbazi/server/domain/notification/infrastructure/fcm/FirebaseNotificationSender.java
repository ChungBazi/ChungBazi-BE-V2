package com.chungbazi.server.domain.notification.infrastructure.fcm;

import com.chungbazi.server.domain.notification.application.dto.NotificationPushMessage;
import com.chungbazi.server.domain.notification.infrastructure.fcm.dto.FirebasePushResult;
import com.chungbazi.server.domain.notification.infrastructure.fcm.dto.FirebasePushTarget;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.SendResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "firebase", name = "enabled", havingValue = "true")
public class FirebaseNotificationSender {

    private static final int FCM_BATCH_SIZE = 500;

    private final FirebaseMessaging firebaseMessaging;

    public FirebasePushResult sendAll(List<FirebasePushTarget> targets) {
        int successCount = 0;
        int failureCount = 0;
        Set<String> invalidFcmTokens = new HashSet<>();

        for (int start = 0; start < targets.size(); start += FCM_BATCH_SIZE) {
            List<FirebasePushTarget> batchTargets = targets.subList(
                    start,
                    Math.min(start + FCM_BATCH_SIZE, targets.size())
            );
            List<Message> messages = batchTargets.stream()
                    .map(this::toFirebaseMessage)
                    .toList();

            try {
                BatchResponse response = firebaseMessaging.sendEach(messages);
                successCount += response.getSuccessCount();
                failureCount += response.getFailureCount();
                collectInvalidTokens(batchTargets, response.getResponses(), invalidFcmTokens);
            } catch (FirebaseMessagingException exception) {
                failureCount += batchTargets.size();
                log.error("FCM 배치 발송 실패. 대상 수={}", batchTargets.size(), exception);
            }
        }

        return FirebasePushResult.of(successCount, failureCount, invalidFcmTokens);
    }

    private Message toFirebaseMessage(FirebasePushTarget target) {
        NotificationPushMessage message = target.message();
        Message.Builder builder = Message.builder()
                .setToken(target.fcmToken())
                .setNotification(com.google.firebase.messaging.Notification.builder()
                        .setTitle(message.title())
                        .setBody(message.message())
                        .build())
                .putData("category", message.category().name())
                .putData("policyId", String.valueOf(message.policyId()));

        if (message.notificationId() != null) {
            builder.putData("notificationId", String.valueOf(message.notificationId()));
        }
        return builder.build();
    }

    private void collectInvalidTokens(
            List<FirebasePushTarget> targets,
            List<SendResponse> responses,
            Set<String> invalidFcmTokens
    ) {
        for (int index = 0; index < responses.size(); index++) {
            SendResponse response = responses.get(index);
            if (!response.isSuccessful() && isUnregistered(response.getException())) {
                invalidFcmTokens.add(targets.get(index).fcmToken());
            }
        }
    }

    private boolean isUnregistered(FirebaseMessagingException exception) {
        return exception != null
                && exception.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED;
    }
}
