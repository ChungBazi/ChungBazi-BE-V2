package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.api.dto.response.PolicyCardListResponse;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyCardResponse;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyDetailResponse;
import com.chungbazi.server.domain.policy.application.mapper.PolicyDisplayMapper;
import com.chungbazi.server.domain.policy.application.mapper.PolicyListResponseAssembler;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.PolicyDetail;
import com.chungbazi.server.domain.policy.domain.entity.RecentViewedPolicy;
import com.chungbazi.server.domain.policy.domain.repository.PolicyDetailRepository;
import com.chungbazi.server.domain.policy.domain.repository.PolicyLikeRepository;
import com.chungbazi.server.domain.policy.domain.repository.RecentViewedPolicyRepository;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.policy.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.exception.PolicyException;
import com.chungbazi.server.domain.user.domain.User;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PolicyDetailService {

    private static final int RELATED_POLICY_SIZE = 5;
    private static final int CARD_SIZE = 20;

    private final PolicyRepository policyRepository;
    private final PolicyDetailRepository policyDetailRepository;
    private final PolicyLikeRepository policyLikeRepository;
    private final RecentViewedPolicyRepository recentViewedPolicyRepository;
    private final PolicyDisplayMapper policyDisplayMapper;
    private final PolicyListResponseAssembler policyListResponseAssembler;
    private final PersonalizedPolicyService personalizedPolicyService;

    @Transactional
    public PolicyCardResponse getPolicyCard(User user, Long policyId) {
        Policy policy = increaseViewCountAndFindPolicy(policyId);

        recentViewedPolicyRepository.save(RecentViewedPolicy.createRecentViewedPolicy(user.getId(), policy));

        Set<Long> likedPolicyIds = findLikedPolicyIds(user, policy, List.of(), List.of());

        return policyDisplayMapper.toCardResponse(policy, likedPolicyIds);
    }

    public PolicyCardListResponse getPolicyCards(User user, PolicyCategoryType category) {
        List<Policy> policies = category == null
                ? personalizedPolicyService.getPersonalizedPolicyEntities(user, CARD_SIZE)
                : personalizedPolicyService.getPersonalizedPolicyEntities(user, category, CARD_SIZE);

        Set<Long> likedPolicyIds = policyListResponseAssembler.findLikedPolicyIds(user.getId(), policies);

        List<PolicyCardResponse> policyCards = policyDisplayMapper.toCardResponses(policies, likedPolicyIds);

        return PolicyCardListResponse.of(policyCards);
    }

    @Transactional
    public PolicyDetailResponse getPolicyDetail(User user, Long policyId) {
        Policy policy = increaseViewCountAndFindPolicy(policyId);

        recentViewedPolicyRepository.save(RecentViewedPolicy.createRecentViewedPolicy(user.getId(), policy));

        PolicyDetail policyDetail = policyDetailRepository.findByPolicyId(policy.getId())
                .orElse(null);
        List<Policy> personalizedPolicies = findPersonalizedPolicies(user, policy);
        List<Policy> popularPolicies = findPopularPoliciesInSameCategory(user, policy);
        Set<Long> likedPolicyIds = findLikedPolicyIds(user, policy, personalizedPolicies, popularPolicies);

        return policyDisplayMapper.toDetailResponse(
                policy,
                policyDetail,
                likedPolicyIds,
                personalizedPolicies,
                popularPolicies
        );
    }

    private Policy increaseViewCountAndFindPolicy(Long policyId) {
        int updatedCount = policyRepository.increaseViewCount(policyId);
        if (updatedCount == 0) {
            throw new PolicyException(PolicyErrorCode.POLICY_NOT_FOUND);
        }
        return findPolicy(policyId);
    }

    private Policy findPolicy(Long policyId) {
        return policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyException(PolicyErrorCode.POLICY_NOT_FOUND));
    }

    private List<Policy> findPopularPoliciesInSameCategory(User user, Policy policy) {
        return policyRepository.findPopularPolicies(
                        policy.getCategory(),
                        RecruitmentStatus.CLOSED,
                        user.getSidoCode(),
                        user.getSigunguCode(),
                        PageRequest.of(0, RELATED_POLICY_SIZE + 1)
                )
                .stream()
                .filter(popularPolicy -> !popularPolicy.getId().equals(policy.getId()))
                .limit(RELATED_POLICY_SIZE)
                .toList();
    }

    private List<Policy> findPersonalizedPolicies(User user, Policy policy) {
        return personalizedPolicyService.getPersonalizedPolicyEntities(user, RELATED_POLICY_SIZE + 1)
                .stream()
                .filter(personalizedPolicy -> !personalizedPolicy.getId().equals(policy.getId()))
                .limit(RELATED_POLICY_SIZE)
                .toList();
    }

    private Set<Long> findLikedPolicyIds(
            User user,
            Policy policy,
            List<Policy> personalizedPolicies,
            List<Policy> popularPolicies
    ) {
        List<Long> policyIds = Stream.of(
                        Stream.of(policy),
                        personalizedPolicies.stream(),
                        popularPolicies.stream()
                )
                .flatMap(stream -> stream)
                .map(Policy::getId)
                .distinct()
                .toList();

        if (policyIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(policyLikeRepository.findLikedPolicyIds(user.getId(), policyIds));
    }
}
