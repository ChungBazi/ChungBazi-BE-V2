package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.application.dto.PolicyRecommendationContext;
import com.chungbazi.server.domain.policy.application.support.PersonalizedPolicyRanker;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalizedPolicyService {

    private static final int CANDIDATE_SIZE = 300;
    private static final int BEHAVIOR_HISTORY_SIZE = 50;

    private final PolicyRepository policyRepository;
    private final UserInterestRepository userInterestRepository;
    private final PolicyLikeRepository policyLikeRepository;
    private final RecentViewedPolicyRepository recentViewedPolicyRepository;
    private final PersonalizedPolicyRanker personalizedPolicyRanker;

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

        return personalizedPolicyRanker.rank(user, context, candidates, size);
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

        return personalizedPolicyRanker.rank(user, context, candidates, size);
    }
}
