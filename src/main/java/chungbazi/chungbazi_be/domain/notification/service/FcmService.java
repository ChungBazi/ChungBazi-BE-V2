package chungbazi.chungbazi_be.domain.notification.service;

import chungbazi.chungbazi_be.domain.notification.dto.internal.FcmPushData;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FcmService {

    //fcm한테 알림 요청
    public void pushFCMNotification(String fcmToken, FcmPushData fcmPushData) {
        try {
            Notification notification =Notification.builder()
                    .setTitle("새로운 알림이 도착했습니다.")
                    .setBody(fcmPushData.message())
                    .build();

            Map<String, String> data = new HashMap<>();
            data.put("targetId", fcmPushData.targetId().toString());
            data.put("notificationType", fcmPushData.type().toString());

            Message firebaseMessage = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification)
                    .putAllData(data)
                    .build();

            String response = FirebaseMessaging.getInstance().send(firebaseMessage);
        }catch(FirebaseMessagingException e){
            e.printStackTrace();
        }
    }
}
