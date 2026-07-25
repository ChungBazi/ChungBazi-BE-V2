package com.chungbazi.server.domain.policy.application.mapper;

import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse.PolicySummary;
import com.chungbazi.server.domain.policy.application.cursor.PolicyCursorParser;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.repository.PolicyLikeRepository;
import com.chungbazi.server.domain.policy.domain.type.PolicySortType;
import com.chungbazi.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PolicyListResponseAssembler {

    private final PolicyLikeRepository policyLikeRepository;
    private final PolicyDisplayMapper policyDisplayMapper;

    public PolicyListResponse assemble(
            User user,
            PolicySortType sort,
            List<Policy> fetchedPolicies,
            Long totalCount,
            int size
    ) {
        boolean hasNext = fetchedPolicies.size() > size;
        List<Policy> policies = hasNext
                ? new ArrayList<>(fetchedPolicies.subList(0, size))
                : fetchedPolicies;

        Set<Long> likedPolicyIds = findLikedPolicyIds(user.getId(), policies);
        String nextCursor = hasNext
                ? PolicyCursorParser.encode(sort, policies.getLast())
                : null;

        return assemble(
                totalCount,
                policyDisplayMapper.toSummaries(policies, likedPolicyIds),
                nextCursor,
                hasNext
        );
    }

    public List<PolicySummary> summarize(List<Policy> policies, Set<Long> likedPolicyIds) {
        return policyDisplayMapper.toSummaries(policies, likedPolicyIds);
    }

    public PolicyListResponse assemble(
            Long totalCount,
            List<PolicySummary> policies,
            String nextCursor,
            boolean hasNext
    ) {
        return new PolicyListResponse(totalCount, policies, nextCursor, hasNext);
    }

    public Set<Long> findLikedPolicyIds(Long userId, List<Policy> policies) {
        if (policies.isEmpty()) {
            return Set.of();
        }

        List<Long> policyIds = policies.stream()
                .map(Policy::getId)
                .toList();

        return new HashSet<>(policyLikeRepository.findLikedPolicyIds(userId, policyIds));
    }
}
