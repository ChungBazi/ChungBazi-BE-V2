package com.chungbazi.server.domain.policy.domain.repository;

import com.chungbazi.server.domain.policy.domain.entity.PolicyLike;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolicyLikeRepository extends JpaRepository<PolicyLike, Long> {

    @Query("""
            SELECT policyLike.policy.id
            FROM PolicyLike policyLike
            WHERE policyLike.userId = :userId
              AND policyLike.policy.id IN :policyIds
            """)
    List<Long> findLikedPolicyIds(
            @Param("userId") Long userId,
            @Param("policyIds") Collection<Long> policyIds
    );
}
