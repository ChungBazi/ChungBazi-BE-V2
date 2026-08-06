package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.PolicyLike;
import com.chungbazi.server.domain.policy.domain.repository.PolicyLikeRepository;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.exception.PolicyException;
import com.chungbazi.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PolicyLikeService {

    private final PolicyRepository policyRepository;
    private final PolicyLikeRepository policyLikeRepository;

    @Transactional
    public void likePolicy(User user, Long policyId) {
        if (policyLikeRepository.existsByUserIdAndPolicy_Id(user.getId(), policyId)) {
            return;
        }

        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyException(PolicyErrorCode.POLICY_NOT_FOUND));

        try {
            policyLikeRepository.saveAndFlush(PolicyLike.createPolicyLike(user.getId(), policy, null));
            policyRepository.increaseSaveCount(policyId);
        } catch (DataIntegrityViolationException ignored) {
            //동시에 다른 사용자들에게 같은 요청이 들어올 경우 방어
        }
    }

    @Transactional
    public void unlikePolicy(User user, Long policyId) {
        long deletedCount = policyLikeRepository.deleteByUserIdAndPolicy_Id(user.getId(), policyId);
        if (deletedCount > 0) {
            policyRepository.decreaseSaveCount(policyId);
        }
    }
}
