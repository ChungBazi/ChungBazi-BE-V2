package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.domain.repository.PolicyLikeRepository;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.exception.PolicyException;
import com.chungbazi.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PolicyLikeService {

    private final PolicyRepository policyRepository;
    private final PolicyLikeRepository policyLikeRepository;

    @Transactional
    public void likePolicy(User user, Long policyId) {
        if (!policyRepository.existsById(policyId)) {
            throw new PolicyException(PolicyErrorCode.POLICY_NOT_FOUND);
        }

        int insertedCount = policyLikeRepository.insertIgnore(user.getId(), policyId);
        if (insertedCount > 0) {
            policyRepository.increaseSaveCount(policyId);
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
