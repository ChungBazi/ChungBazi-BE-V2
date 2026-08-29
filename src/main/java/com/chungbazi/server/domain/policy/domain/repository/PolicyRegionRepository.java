package com.chungbazi.server.domain.policy.domain.repository;

import com.chungbazi.server.domain.policy.domain.entity.PolicyRegion;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolicyRegionRepository extends JpaRepository<PolicyRegion, Long> {

    @Query("""
            SELECT policyRegion
            FROM PolicyRegion policyRegion
            JOIN FETCH policyRegion.policy policy
            LEFT JOIN FETCH policyRegion.regionCode
            WHERE policy.id IN :policyIds
            """)
    List<PolicyRegion> findAllByPolicyIds(
            @Param("policyIds") Collection<Long> policyIds
    );

    @Modifying
    @Query("""
            DELETE FROM PolicyRegion policyRegion
            WHERE policyRegion.policy.id = :policyId
            """)
    void deleteAllByPolicyId(@Param("policyId") Long policyId);
}
