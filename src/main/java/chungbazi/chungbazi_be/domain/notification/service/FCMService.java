package chungbazi.chungbazi_be.domain.notification.service;

import chungbazi.chungbazi_be.domain.notification.dto.internal.FcmPushData;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class FCMService {
    private final RedisTemplate<String, String> redisTemplate;

    public void saveFcmToken(Long userId, String fcmToken) {
        redisTemplate.opsForValue().set("_"+String.valueOf(userId), fcmToken);
    }

    public String getToken(Long userId){
        return redisTemplate.opsForValue().get("_"+String.valueOf(userId));
    }

    public void deleteToken(Long userId){
        redisTemplate.delete(String.valueOf("_"+userId));
    }


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
