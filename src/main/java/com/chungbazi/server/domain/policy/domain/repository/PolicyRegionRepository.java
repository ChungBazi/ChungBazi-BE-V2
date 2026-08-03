package com.chungbazi.server.domain.policy.domain.repository;

import com.chungbazi.server.domain.policy.domain.entity.PolicyRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolicyRegionRepository extends JpaRepository<PolicyRegion, Long> {

    @Modifying
    @Query("""
            DELETE FROM PolicyRegion policyRegion
            WHERE policyRegion.policy.id = :policyId
            """)
    void deleteAllByPolicyId(@Param("policyId") Long policyId);
}
