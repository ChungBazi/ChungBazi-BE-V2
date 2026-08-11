package com.chungbazi.server.domain.policy.domain.repository;

import com.chungbazi.server.domain.policy.domain.entity.RecentViewedPolicy;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.policy.domain.type.SidoCode;
import java.time.LocalDateTime;
import java.util.List;

import com.chungbazi.server.domain.user.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecentViewedPolicyRepository extends JpaRepository<RecentViewedPolicy, Long> {

    @Query("""
            SELECT recentViewedPolicy
            FROM RecentViewedPolicy recentViewedPolicy
            JOIN FETCH recentViewedPolicy.policy
            WHERE recentViewedPolicy.userId = :userId
              AND recentViewedPolicy.policy.recruitmentStatus <> :closedStatus
              AND (
                    recentViewedPolicy.policy.national = true
                    OR EXISTS (
                        SELECT policyRegion.id
                        FROM PolicyRegion policyRegion
                        WHERE policyRegion.policy = recentViewedPolicy.policy
                          AND policyRegion.sidoCode = :sidoCode
                          AND (
                                policyRegion.regionCode IS NULL
                                OR (
                                    :sigunguCode IS NOT NULL
                                    AND :sigunguCode <> ''
                                    AND policyRegion.regionCode.sigunguCode = :sigunguCode
                                )
                          )
                    )
              )
              AND recentViewedPolicy.viewedAt = (
                    SELECT MAX(latestRecentViewedPolicy.viewedAt)
                    FROM RecentViewedPolicy latestRecentViewedPolicy
                    WHERE latestRecentViewedPolicy.userId = :userId
                      AND latestRecentViewedPolicy.policy = recentViewedPolicy.policy
            )
            ORDER BY recentViewedPolicy.viewedAt DESC, recentViewedPolicy.policy.id DESC
            """)
    List<RecentViewedPolicy> findRecentViewedPolicies(
            @Param("userId") Long userId,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("sidoCode") SidoCode sidoCode,
            @Param("sigunguCode") String sigunguCode,
            Pageable pageable
    );

    @Query("""
            SELECT recentViewedPolicy
            FROM RecentViewedPolicy recentViewedPolicy
            JOIN FETCH recentViewedPolicy.policy
            WHERE recentViewedPolicy.userId = :userId
              AND recentViewedPolicy.policy.recruitmentStatus <> :closedStatus
              AND (
                    recentViewedPolicy.policy.national = true
                    OR EXISTS (
                        SELECT policyRegion.id
                        FROM PolicyRegion policyRegion
                        WHERE policyRegion.policy = recentViewedPolicy.policy
                          AND policyRegion.sidoCode = :sidoCode
                          AND (
                                policyRegion.regionCode IS NULL
                                OR (
                                    :sigunguCode IS NOT NULL
                                    AND :sigunguCode <> ''
                                    AND policyRegion.regionCode.sigunguCode = :sigunguCode
                                )
                          )
                    )
              )
              AND recentViewedPolicy.viewedAt = (
                    SELECT MAX(latestRecentViewedPolicy.viewedAt)
                    FROM RecentViewedPolicy latestRecentViewedPolicy
                    WHERE latestRecentViewedPolicy.userId = :userId
                      AND latestRecentViewedPolicy.policy = recentViewedPolicy.policy
              )
              AND (
                    recentViewedPolicy.viewedAt < :viewedAt
                    OR (
                        recentViewedPolicy.viewedAt = :viewedAt
                        AND recentViewedPolicy.policy.id < :policyId
                    )
              )
            ORDER BY recentViewedPolicy.viewedAt DESC, recentViewedPolicy.policy.id DESC
            """)
    List<RecentViewedPolicy> findRecentViewedPoliciesAfter(
            @Param("userId") Long userId,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("sidoCode") SidoCode sidoCode,
            @Param("sigunguCode") String sigunguCode,
            @Param("viewedAt") LocalDateTime viewedAt,
            @Param("policyId") Long policyId,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(DISTINCT recentViewedPolicy.policy.id)
            FROM RecentViewedPolicy recentViewedPolicy
            WHERE recentViewedPolicy.userId = :userId
              AND recentViewedPolicy.policy.recruitmentStatus <> :closedStatus
              AND (
                    recentViewedPolicy.policy.national = true
                    OR EXISTS (
                        SELECT policyRegion.id
                        FROM PolicyRegion policyRegion
                        WHERE policyRegion.policy = recentViewedPolicy.policy
                          AND policyRegion.sidoCode = :sidoCode
                          AND (
                                policyRegion.regionCode IS NULL
                                OR (
                                    :sigunguCode IS NOT NULL
                                    AND :sigunguCode <> ''
                                    AND policyRegion.regionCode.sigunguCode = :sigunguCode
                                )
                          )
                    )
              )
            """)
    long countRecentViewedPolicies(
            @Param("userId") Long userId,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("sidoCode") SidoCode sidoCode,
            @Param("sigunguCode") String sigunguCode
    );

    void deleteAllByUserId(Long userId);
}
