package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.application.dto.PolicyRecommendationContext;
import com.chungbazi.server.domain.policy.application.support.PersonalizedPolicyRanker;
import com.chungbazi.server.domain.policy.application.support.RecentSearchPolicyScoreCalculator;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.repository.PolicyLikeRepository;
import com.chungbazi.server.domain.policy.domain.repository.RecentViewedPolicyRepository;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.UserInterest;
import com.chungbazi.server.domain.user.domain.UserSpecialEligibility;
import com.chungbazi.server.domain.user.domain.type.SpecialEligibilityType;
import com.chungbazi.server.domain.user.infrastructure.UserInterestRepository;
import com.chungbazi.server.domain.user.infrastructure.UserSpecialEligibilityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalizedPolicyService {

    private static final int RECENT_LIKE_SIZE = 5;
    private static final int RECENT_VIEW_SIZE = 50;

    private final PolicyRepository policyRepository;
    private final UserInterestRepository userInterestRepository;
    private final UserSpecialEligibilityRepository userSpecialEligibilityRepository;
    private final PolicyLikeRepository policyLikeRepository;
    private final RecentViewedPolicyRepository recentViewedPolicyRepository;
    private final RecentSearchPolicyScoreCalculator recentSearchPolicyScoreCalculator;
    private final PersonalizedPolicyRanker personalizedPolicyRanker;

    public List<Policy> getPersonalizedPolicies(User user, int size) {
        return findPolicies(user, size);
    }

    public List<Policy> getPolicyCards(User user, int size) {
        return getPersonalizedPolicies(user, size);
    }

    public List<Policy> getPersonalizedPoliciesByCategory(User user, PolicyCategoryType category, int size) {
        return findByCategory(user, category, size);
    }

    public List<Policy> getPolicyCardsByCategory(User user, PolicyCategoryType category, int size) {
        return getPersonalizedPoliciesByCategory(user, category, size);
    }

    private List<Policy> findPolicies(User user, int size) {
        List<UserInterest> interests = userInterestRepository.findAllByUser(user);
        Set<SpecialEligibilityType> eligibilityTypes = findSpecialEligibilityTypes(user);

        List<Policy> candidates = policyRepository.findEligiblePolicies(
                null,
                RecruitmentStatus.CLOSED,
                user.getSidoCode(),
                user.getSigunguCode(),
                eligibilityTypes
        );

        PolicyRecommendationContext context = createRecommendationContext(user, interests);
        return personalizedPolicyRanker.rank(user, context, candidates, size);
    }

    private List<Policy> findByCategory(User user, PolicyCategoryType category, int size) {
        List<UserInterest> interests = userInterestRepository.findAllByUser(user);
        boolean interestedCategory = interests.stream()
                .anyMatch(interest -> interest.getCategory() == category);

        // 선택하지 않은 카테고리면 빈 리스트 반환
        if (!interestedCategory) {
            return List.of();
        }

        Set<SpecialEligibilityType> eligibilityTypes = findSpecialEligibilityTypes(user);

        List<Policy> candidates = policyRepository.findEligiblePolicies(
                category,
                RecruitmentStatus.CLOSED,
                user.getSidoCode(),
                user.getSigunguCode(),
                eligibilityTypes
        );

        PolicyRecommendationContext context = createRecommendationContext(user, interests);
        return personalizedPolicyRanker.rank(user, context, candidates, size);
    }

    private Set<SpecialEligibilityType> findSpecialEligibilityTypes(User user) {
        Set<SpecialEligibilityType> eligibilityTypes = userSpecialEligibilityRepository
                .findAllByUser(user)
                .stream()
                .map(UserSpecialEligibility::getEligibilityType)
                .collect(Collectors.toSet());

        return eligibilityTypes.isEmpty()
                ? Set.of(SpecialEligibilityType.NONE)
                : eligibilityTypes;
    }

    private PolicyRecommendationContext createRecommendationContext(User user, List<UserInterest> interests) {
        return PolicyRecommendationContext.of(
                interests,
                policyLikeRepository.findRecentPolicyLikesWithPolicy(
                        user.getId(),
                        PageRequest.of(0, RECENT_LIKE_SIZE)
                ),
                recentViewedPolicyRepository.findRecentViewedPolicyEvents(
                        user.getId(),
                        PageRequest.of(0, RECENT_VIEW_SIZE)
                ),
                findRecentSearchScores(user)
        );
    }

    private Map<Long, Integer> findRecentSearchScores(User user) {
        return user.isSearchKeywordAutoSaveEnabled()
                ? recentSearchPolicyScoreCalculator.calculateScores(user.getId())
                : Map.of();
    }
}
