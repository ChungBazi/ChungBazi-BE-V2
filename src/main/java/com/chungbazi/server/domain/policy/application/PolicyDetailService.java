package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.api.dto.response.PolicyCardResponse;
import com.chungbazi.server.domain.policy.application.mapper.PolicyDisplayMapper;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.RecentViewedPolicy;
import com.chungbazi.server.domain.policy.domain.repository.PolicyLikeRepository;
import com.chungbazi.server.domain.policy.domain.repository.RecentViewedPolicyRepository;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.exception.PolicyException;
import com.chungbazi.server.domain.user.domain.User;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PolicyDetailService {

    private final PolicyRepository policyRepository;
    private final PolicyLikeRepository policyLikeRepository;
    private final RecentViewedPolicyRepository recentViewedPolicyRepository;
    private final PolicyDisplayMapper policyDisplayMapper;

    @Transactional
    public PolicyCardResponse getPolicyCard(User user, Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyException(PolicyErrorCode.POLICY_NOT_FOUND));

        policy.increaseViewCount();
        recentViewedPolicyRepository.save(RecentViewedPolicy.createRecentViewedPolicy(user.getId(), policy));

        Set<Long> likedPolicyIds = Set.copyOf(
                policyLikeRepository.findLikedPolicyIds(user.getId(), List.of(policy.getId()))
        );

        return policyDisplayMapper.toCardResponse(policy, likedPolicyIds);
    }
}
