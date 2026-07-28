package com.chungbazi.server.domain.policy.application.mapper;

import com.chungbazi.server.domain.policy.api.dto.response.HomePolicyResponse;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.repository.PolicyLikeRepository;
import com.chungbazi.server.domain.user.domain.User;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HomePolicyResponseAssembler {

    private final PolicyLikeRepository policyLikeRepository;
    private final PolicyDisplayMapper policyDisplayMapper;

    public HomePolicyResponse assemble(
            User user,
            List<Policy> personalizedPolicies,
            List<Policy> recentViewedPolicies,
            List<Policy> popularPolicies,
            List<Policy> upcomingDeadlinePolicies,
            List<Policy> latestPolicies
    ) {
        Set<Long> likedPolicyIds = findLikedPolicyIds(
                user.getId(),
                personalizedPolicies,
                recentViewedPolicies,
                popularPolicies,
                upcomingDeadlinePolicies,
                latestPolicies
        );

        return new HomePolicyResponse(
                policyDisplayMapper.toSummaries(personalizedPolicies, likedPolicyIds),
                policyDisplayMapper.toSummaries(recentViewedPolicies, likedPolicyIds),
                policyDisplayMapper.toSummaries(popularPolicies, likedPolicyIds),
                policyDisplayMapper.toSummaries(upcomingDeadlinePolicies, likedPolicyIds),
                policyDisplayMapper.toSummaries(latestPolicies, likedPolicyIds)
        );
    }

    @SafeVarargs
    private Set<Long> findLikedPolicyIds(Long userId, List<Policy>... policySections) {
        List<Long> policyIds = Stream.of(policySections)
                .flatMap(List::stream)
                .map(Policy::getId)
                .distinct()
                .toList();

        if (policyIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(policyLikeRepository.findLikedPolicyIds(userId, policyIds));
    }
}
