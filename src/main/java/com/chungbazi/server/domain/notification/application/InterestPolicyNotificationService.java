package com.chungbazi.server.domain.notification.application;

import com.chungbazi.server.domain.notification.application.dto.InterestPolicyNotificationTarget;
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

    public void createInterestPolicyNotifications(NewPoliciesRegisteredEvent event) {
        List<Policy> newPolicies = policyRepository.findAllByIdInAndRecruitmentStatusNot(
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

            // TODO: 관심 분야 신규 정책 알림 저장
            cursor = users.getLast().getId();
        }
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
}
