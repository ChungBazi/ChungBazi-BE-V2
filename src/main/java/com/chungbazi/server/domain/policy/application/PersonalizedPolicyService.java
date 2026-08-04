package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.application.dto.PolicyRecommendationContext;
import com.chungbazi.server.domain.policy.application.support.PersonalizedPolicyScorer;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.repository.PolicyLikeRepository;
import com.chungbazi.server.domain.policy.domain.repository.RecentViewedPolicyRepository;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.UserInterest;
import com.chungbazi.server.domain.user.infrastructure.UserInterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalizedPolicyService {

    private static final int CANDIDATE_SIZE = 300;
    private static final int BEHAVIOR_HISTORY_SIZE = 50;
    private static final int MAX_SAME_CATEGORY_COUNT = 3;

    private final PolicyRepository policyRepository;
    private final UserInterestRepository userInterestRepository;
    private final PolicyLikeRepository policyLikeRepository;
    private final RecentViewedPolicyRepository recentViewedPolicyRepository;
    private final PersonalizedPolicyScorer scorer;

    public List<Policy> getPersonalizedPolicyEntities(User user, int size) {
        // TODO: 추후 캐싱 고려
        // TODO: 실제 정책 데이터와 추천 결과를 확인한 뒤 후보군 조회 기준 재조정
        List<Policy> candidates = policyRepository.findAllLatestPolicies(
                RecruitmentStatus.CLOSED,
                user.getSidoCode(),
                user.getSigunguCode(),
                PageRequest.of(0, CANDIDATE_SIZE)
        );

        PolicyRecommendationContext context = PolicyRecommendationContext.of(
                userInterestRepository.findAllByUser(user),
                policyLikeRepository.findRecentPolicyLikesWithPolicy(
                        user.getId(),
                        PageRequest.of(0, BEHAVIOR_HISTORY_SIZE)
                ),
                recentViewedPolicyRepository.findRecentViewedPolicies(
                        user.getId(),
                        RecruitmentStatus.CLOSED,
                        user.getSidoCode(),
                        user.getSigunguCode(),
                        PageRequest.of(0, BEHAVIOR_HISTORY_SIZE)
                )
        );

        List<Policy> scoredPolicies = candidates.stream()
                .filter(policy -> scorer.isEligible(user, policy))
                .sorted(Comparator
                        .comparingInt((Policy policy) -> scorer.score(user, context, policy))
                        .reversed()
                        .thenComparing(Policy::getRegisteredAt, Comparator.reverseOrder()))
                .toList();

        // 관심 대분류를 하나만 선택한 경우, 카테고리 다양성 제한 없이 점수순 그대로
        if (context.interestCategoryCounts().size() <= 1) {
            return scoredPolicies.stream()
                    .limit(size)
                    .toList();
        }
        return diversifyByCategory(scoredPolicies, size);
    }

    public List<Policy> getPersonalizedPolicyEntities(User user, PolicyCategoryType category, int size) {
        List<UserInterest> interests = userInterestRepository.findAllByUser(user);
        boolean interestedCategory = interests.stream()
                .anyMatch(interest -> interest.getCategory() == category);

        // 선택하지 않은 카테고리면 빈 리스트 반환
        if (!interestedCategory) {
            return List.of();
        }

        List<Policy> candidates = policyRepository.findLatestPolicies(
                category,
                RecruitmentStatus.CLOSED,
                user.getSidoCode(),
                user.getSigunguCode(),
                PageRequest.of(0, CANDIDATE_SIZE)
        );

        PolicyRecommendationContext context = PolicyRecommendationContext.of(
                interests,
                policyLikeRepository.findRecentPolicyLikesWithPolicy(
                        user.getId(),
                        PageRequest.of(0, BEHAVIOR_HISTORY_SIZE)
                ),
                recentViewedPolicyRepository.findRecentViewedPolicies(
                        user.getId(),
                        RecruitmentStatus.CLOSED,
                        user.getSidoCode(),
                        user.getSigunguCode(),
                        PageRequest.of(0, BEHAVIOR_HISTORY_SIZE)
                )
        );

        return candidates.stream()
                .filter(policy -> scorer.isEligible(user, policy))
                .sorted(Comparator
                        .comparingInt((Policy policy) -> scorer.score(user, context, policy))
                        .reversed()
                        .thenComparing(Policy::getRegisteredAt, Comparator.reverseOrder()))
                .limit(size)
                .toList();
    }

    private List<Policy> diversifyByCategory(List<Policy> policies, int size) {
        List<Policy> selectedPolicies = new ArrayList<>();
        Map<PolicyCategoryType, Integer> selectedCategoryCounts = new EnumMap<>(PolicyCategoryType.class);

        // 관심 대분류가 여러 개인 경우, 같은 대분류가 과도하게 몰리지 않도록 노출 개수 제한
        for (Policy policy : policies) {
            PolicyCategoryType category = policy.getCategory();
            int categoryCount = selectedCategoryCounts.getOrDefault(category, 0);

            if (categoryCount >= MAX_SAME_CATEGORY_COUNT) {
                continue;
            }
            selectedPolicies.add(policy);
            selectedCategoryCounts.put(category, categoryCount + 1);

            if (selectedPolicies.size() == size) {
                return selectedPolicies;
            }
        }

        // 제한 때문에 목표 개수를 채우지 못한 경우에는 점수순으로 남은 정책 추가
        for (Policy policy : policies) {
            if (selectedPolicies.contains(policy)) {
                continue;
            }
            selectedPolicies.add(policy);

            if (selectedPolicies.size() == size) {
                return selectedPolicies;
            }
        }
        return selectedPolicies;
    }
}
