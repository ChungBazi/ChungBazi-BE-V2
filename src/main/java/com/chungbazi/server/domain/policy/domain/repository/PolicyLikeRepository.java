package com.chungbazi.server.domain.policy.domain.repository;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.PolicyLike;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentType;
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

    @Modifying
    @Query("""
            UPDATE PolicyLike policyLike
            SET policyLike.memo = :memo,
                policyLike.updatedAt = :updatedAt
            WHERE policyLike.userId = :userId
              AND policyLike.policy.id = :policyId
            """)
    int updateMemo(
            @Param("userId") Long userId,
            @Param("policyId") Long policyId,
            @Param("memo") String memo,
            @Param("updatedAt") LocalDateTime updatedAt
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
              AND policy.recruitmentType <> :openEndedType
              AND policy.applyEndDate IS NOT NULL
              AND policy.applyEndDate >= :today
            ORDER BY policy.applyEndDate ASC, policy.id DESC
            """)
    List<Policy> findUpcomingDeadlineLikedPolicies(
            @Param("userId") Long userId,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("openEndedType") RecruitmentType openEndedType,
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

    @Query("""
            SELECT DISTINCT policy.applyEndDate
            FROM PolicyLike policyLike
            JOIN policyLike.policy policy
            WHERE policyLike.userId = :userId
              AND policy.recruitmentStatus <> :closedStatus
              AND policy.applyEndDate BETWEEN :startDate AND :endDate
            ORDER BY policy.applyEndDate ASC
            """)
    List<LocalDate> findDistinctLikedPolicyDeadlineDates(
            @Param("userId") Long userId,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT COUNT(policyLike)
            FROM PolicyLike policyLike
            JOIN policyLike.policy policy
            WHERE policyLike.userId = :userId
              AND policy.recruitmentStatus <> :closedStatus
              AND policy.recruitmentType = :recruitmentType
            """)
    Long countOpenEndedLikedPolicies(
            @Param("userId") Long userId,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("recruitmentType") RecruitmentType recruitmentType
    );

    @Query("""
            SELECT policyLike
            FROM PolicyLike policyLike
            JOIN FETCH policyLike.policy policy
            WHERE policyLike.userId = :userId
              AND policy.recruitmentStatus <> :closedStatus
              AND policy.recruitmentType = :recruitmentType
            ORDER BY policyLike.id DESC
            """)
    List<PolicyLike> findOpenEndedLikedPoliciesFirst(
            @Param("userId") Long userId,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("recruitmentType") RecruitmentType recruitmentType,
            Pageable pageable
    );

    @Query("""
            SELECT policyLike
            FROM PolicyLike policyLike
            JOIN FETCH policyLike.policy policy
            WHERE policyLike.userId = :userId
              AND policy.recruitmentStatus <> :closedStatus
              AND policy.recruitmentType = :recruitmentType
              AND policyLike.id < :policyLikeId
            ORDER BY policyLike.id DESC
            """)
    List<PolicyLike> findOpenEndedLikedPoliciesAfter(
            @Param("userId") Long userId,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("recruitmentType") RecruitmentType recruitmentType,
            @Param("policyLikeId") Long policyLikeId,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(policy)
            FROM PolicyLike policyLike
            JOIN policyLike.policy policy
            WHERE policyLike.userId = :userId
              AND policy.recruitmentStatus <> :closedStatus
              AND (:category IS NULL OR policy.category = :category)
            """)
    Long countMyLikedPolicies(
            @Param("userId") Long userId,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("category") PolicyCategoryType category
    );

    @Query("""
            SELECT policy
            FROM PolicyLike policyLike
            JOIN policyLike.policy policy
            WHERE policyLike.userId = :userId
              AND policy.recruitmentStatus <> :closedStatus
              AND (:category IS NULL OR policy.category = :category)
            ORDER BY policy.registeredAt DESC, policy.id DESC
            """)
    List<Policy> findMyLikedPoliciesOrderByLatestFirst(
            @Param("userId") Long userId,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("category") PolicyCategoryType category,
            Pageable pageable
    );

    @Query("""
            SELECT policy
            FROM PolicyLike policyLike
            JOIN policyLike.policy policy
            WHERE policyLike.userId = :userId
              AND policy.recruitmentStatus <> :closedStatus
              AND (:category IS NULL OR policy.category = :category)
              AND (
                  policy.registeredAt < :registeredAt
                  OR (policy.registeredAt = :registeredAt AND policy.id < :policyId)
              )
            ORDER BY policy.registeredAt DESC, policy.id DESC
            """)
    List<Policy> findMyLikedPoliciesOrderByLatestAfter(
            @Param("userId") Long userId,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("category") PolicyCategoryType category,
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
              AND (:category IS NULL OR policy.category = :category)
            ORDER BY
              CASE WHEN policy.recruitmentType = :openEndedType OR policy.applyEndDate IS NULL THEN 1 ELSE 0 END ASC,
              CASE WHEN policy.recruitmentType = :openEndedType OR policy.applyEndDate IS NULL THEN NULL ELSE policy.applyEndDate END ASC,
              policy.id DESC
            """)
    List<Policy> findMyLikedPoliciesOrderByDeadlineFirst(
            @Param("userId") Long userId,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("category") PolicyCategoryType category,
            @Param("openEndedType") RecruitmentType openEndedType,
            Pageable pageable
    );

    @Query("""
            SELECT policy
            FROM PolicyLike policyLike
            JOIN policyLike.policy policy
            WHERE policyLike.userId = :userId
              AND policy.recruitmentStatus <> :closedStatus
              AND (:category IS NULL OR policy.category = :category)
              AND (
                  policy.applyEndDate > :applyEndDate
                  OR (policy.applyEndDate = :applyEndDate AND policy.id < :policyId)
                  OR policy.applyEndDate IS NULL
                  OR policy.recruitmentType = :openEndedType
              )
            ORDER BY
              CASE WHEN policy.recruitmentType = :openEndedType OR policy.applyEndDate IS NULL THEN 1 ELSE 0 END ASC,
              CASE WHEN policy.recruitmentType = :openEndedType OR policy.applyEndDate IS NULL THEN NULL ELSE policy.applyEndDate END ASC,
              policy.id DESC
            """)
    List<Policy> findMyLikedPoliciesOrderByDeadlineAfterDatedCursor(
            @Param("userId") Long userId,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("category") PolicyCategoryType category,
            @Param("openEndedType") RecruitmentType openEndedType,
            @Param("applyEndDate") LocalDate applyEndDate,
            @Param("policyId") Long policyId,
            Pageable pageable
    );

    @Query("""
            SELECT policy
            FROM PolicyLike policyLike
            JOIN policyLike.policy policy
            WHERE policyLike.userId = :userId
              AND policy.recruitmentStatus <> :closedStatus
              AND (:category IS NULL OR policy.category = :category)
              AND (policy.recruitmentType = :openEndedType OR policy.applyEndDate IS NULL)
              AND policy.id < :policyId
            ORDER BY policy.id DESC
            """)
    List<Policy> findMyLikedPoliciesOrderByDeadlineAfterOpenEndedCursor(
            @Param("userId") Long userId,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("category") PolicyCategoryType category,
            @Param("openEndedType") RecruitmentType openEndedType,
            @Param("policyId") Long policyId,
            Pageable pageable
    );
}
