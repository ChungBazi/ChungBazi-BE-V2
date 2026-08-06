package com.chungbazi.server.domain.policy.domain.repository;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.PolicyLike;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Query("""
            SELECT policy
            FROM PolicyLike policyLike
            JOIN policyLike.policy policy
            WHERE policyLike.userId = :userId
              AND policy.recruitmentStatus <> :closedStatus
              AND policy.applyEndDate IS NOT NULL
              AND policy.applyEndDate >= :today
            ORDER BY policy.applyEndDate ASC, policy.id DESC
            """)
    List<Policy> findUpcomingDeadlineLikedPolicies(
            @Param("userId") Long userId,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("today") LocalDate today,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(policy)
            FROM PolicyLike policyLike
            JOIN policyLike.policy policy
            WHERE policyLike.userId = :userId
              AND policy.recruitmentStatus <> :closedStatus
              AND policy.applyEndDate = :targetDate
            """)
    Long countDeadlineLikedPoliciesByDate(
            @Param("userId") Long userId,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("targetDate") LocalDate targetDate
    );

    @Query("""
            SELECT policy
            FROM PolicyLike policyLike
            JOIN policyLike.policy policy
            WHERE policyLike.userId = :userId
              AND policy.recruitmentStatus <> :closedStatus
              AND policy.applyEndDate = :targetDate
            ORDER BY policy.registeredAt DESC, policy.id DESC
            """)
    List<Policy> findDeadlineLikedPoliciesByDateOrderByLatestFirst(
            @Param("userId") Long userId,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("targetDate") LocalDate targetDate,
            Pageable pageable
    );

    @Query("""
            SELECT policy
            FROM PolicyLike policyLike
            JOIN policyLike.policy policy
            WHERE policyLike.userId = :userId
              AND policy.recruitmentStatus <> :closedStatus
              AND policy.applyEndDate = :targetDate
              AND (
                  policy.registeredAt < :registeredAt
                  OR (policy.registeredAt = :registeredAt AND policy.id < :policyId)
              )
            ORDER BY policy.registeredAt DESC, policy.id DESC
            """)
    List<Policy> findDeadlineLikedPoliciesByDateOrderByLatestAfter(
            @Param("userId") Long userId,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("targetDate") LocalDate targetDate,
            @Param("registeredAt") LocalDateTime registeredAt,
            @Param("policyId") Long policyId,
            Pageable pageable
    );

    @Query("""
            SELECT policy
            FROM PolicyLike policyLike
            JOIN policyLike.policy policy
            WHERE policyLike.userId = :userId
              AND policy.recruitmentStatus <> :closedStatus
              AND policy.applyEndDate = :targetDate
            ORDER BY policy.applyEndDate ASC, policy.id DESC
            """)
    List<Policy> findDeadlineLikedPoliciesByDateOrderByDeadlineFirst(
            @Param("userId") Long userId,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("targetDate") LocalDate targetDate,
            Pageable pageable
    );

    @Query("""
            SELECT policy
            FROM PolicyLike policyLike
            JOIN policyLike.policy policy
            WHERE policyLike.userId = :userId
              AND policy.recruitmentStatus <> :closedStatus
              AND policy.applyEndDate = :targetDate
              AND policy.id < :policyId
            ORDER BY policy.applyEndDate ASC, policy.id DESC
            """)
    List<Policy> findDeadlineLikedPoliciesByDateOrderByDeadlineAfter(
            @Param("userId") Long userId,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("targetDate") LocalDate targetDate,
            @Param("policyId") Long policyId,
            Pageable pageable
    );
}
