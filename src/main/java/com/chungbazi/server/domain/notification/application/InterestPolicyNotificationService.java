package com.chungbazi.server.domain.notification.application;

import com.chungbazi.server.domain.policy.application.event.NewPoliciesRegisteredEvent;
import org.springframework.stereotype.Service;

@Service
public class InterestPolicyNotificationService {

    public void createInterestPolicyNotifications(NewPoliciesRegisteredEvent event) {
        // TODO: 신규 정책의 세부 카테고리와 일치하는 관심 분야 사용자 조회
    }
}
