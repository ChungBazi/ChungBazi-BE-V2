package com.chungbazi.server.domain.policy.domain.repository.policyRepository;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolicyRepository extends JpaRepository<Policy, Long>, PolicyRepositoryCustom {
    Optional<Policy> findByPlcyNo(String plcyNo);

    boolean existsByPlcyNo(String plcyNo);

    @Modifying
    @Query("""
            UPDATE Policy policy
            SET policy.viewCount = policy.viewCount + 1
            WHERE policy.id = :policyId
            """)
    int increaseViewCount(@Param("policyId") Long policyId);
}
