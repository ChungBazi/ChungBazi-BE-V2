package com.chungbazi.server.domain.notification.application.mapper;

import com.chungbazi.server.domain.policy.application.dto.PolicyRecommendationContext;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.UserInterest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PersonalizedPolicyContextMapper {

    public Map<Long, PolicyRecommendationContext> toContexts(
            List<User> users,
            List<UserInterest> interests
    ) {
        Map<Long, List<UserInterest>> interestsByUserId = groupInterests(interests);

        Map<Long, PolicyRecommendationContext> contexts = new HashMap<>();
        users.forEach(user -> contexts.put(
                user.getId(),
                PolicyRecommendationContext.of(
                        interestsByUserId.getOrDefault(user.getId(), List.of()),
                        List.of(),
                        List.of()
                )
        ));
        return contexts;
    }

    private Map<Long, List<UserInterest>> groupInterests(List<UserInterest> interests) {
        Map<Long, List<UserInterest>> interestsByUserId = new HashMap<>();
        interests.forEach(interest -> interestsByUserId
                .computeIfAbsent(interest.getUser().getId(), key -> new ArrayList<>())
                .add(interest));
        return interestsByUserId;
    }
}
