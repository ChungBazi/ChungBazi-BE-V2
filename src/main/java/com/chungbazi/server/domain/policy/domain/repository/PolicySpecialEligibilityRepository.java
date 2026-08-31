package com.chungbazi.server.domain.policy.domain.repository;

import com.chungbazi.server.domain.policy.domain.entity.PolicySpecialEligibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolicySpecialEligibilityRepository extends JpaRepository<PolicySpecialEligibility, Long> {

    @Modifying(flushAutomatically = true)
    @Query("""
            DELETE FROM PolicySpecialEligibility policySpecialEligibility
            WHERE policySpecialEligibility.policy.id = :policyId
            """)
    void deleteAllByPolicyId(@Param("policyId") Long policyId);
}
