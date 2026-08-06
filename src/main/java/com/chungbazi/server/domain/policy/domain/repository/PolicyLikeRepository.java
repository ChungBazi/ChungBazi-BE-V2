package com.chungbazi.server.domain.policy.domain.repository;

import com.chungbazi.server.domain.policy.domain.entity.PolicyLike;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolicyLikeRepository extends JpaRepository<PolicyLike, Long> {

    long deleteByUserIdAndPolicy_Id(Long userId, Long policyId);

    @Modifying
    @Query(value = """
            INSERT IGNORE INTO policy_like (user_id, policy_id, created_at, updated_at)
            VALUES (:userId, :policyId, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            """, nativeQuery = true)
    int insertIgnore(
            @Param("userId") Long userId,
            @Param("policyId") Long policyId
    );

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

    @Query("""
            SELECT policyLike
            FROM PolicyLike policyLike
            JOIN FETCH policyLike.policy
            WHERE policyLike.userId = :userId
            ORDER BY policyLike.createdAt DESC
            """)
    List<PolicyLike> findRecentPolicyLikesWithPolicy(
            @Param("userId") Long userId,
            Pageable pageable
    );
}
