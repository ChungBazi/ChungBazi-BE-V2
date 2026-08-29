package com.chungbazi.server.domain.policy.domain.repository;

import com.chungbazi.server.domain.policy.domain.entity.PolicyDetail;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyDetailRepository extends JpaRepository<PolicyDetail, Long> {

    Optional<PolicyDetail> findByPolicyId(Long policyId);
}
