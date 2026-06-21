package com.chungbazi.server.domain.policy.domain.repository;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.policy.domain.type.SidoCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// TODO : QueryDSL로 중복 로직 리팩토링
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    Optional<Policy> findByPlcyNo(String plcyNo);

    boolean existsByPlcyNo(String plcyNo);

    @Query("""
            SELECT COUNT(p)
            FROM Policy p
            WHERE p.category = :category
              AND p.recruitmentStatus <> :closedStatus
              AND (p.national = true OR EXISTS (
                    SELECT policyRegion.id
                    FROM PolicyRegion policyRegion
                    WHERE policyRegion.policy = p
                      AND policyRegion.sidoCode = :sidoCode
                      AND (policyRegion.regionCode IS NULL
                           OR policyRegion.regionCode.sigunguCode = :sigunguCode)
              ))
            """)
    long countVisiblePoliciesByCategory(
            @Param("category") PolicyCategoryType category,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("sidoCode") SidoCode sidoCode,
            @Param("sigunguCode") String sigunguCode
    );

    @Query("""
            SELECT COUNT(p)
            FROM Policy p
            WHERE p.recruitmentStatus <> :closedStatus
              AND (p.national = true OR EXISTS (
                    SELECT policyRegion.id
                    FROM PolicyRegion policyRegion
                    WHERE policyRegion.policy = p
                      AND policyRegion.sidoCode = :sidoCode
                      AND (policyRegion.regionCode IS NULL
                           OR policyRegion.regionCode.sigunguCode = :sigunguCode)
              ))
            """)
    long countVisiblePolicies(
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("sidoCode") SidoCode sidoCode,
            @Param("sigunguCode") String sigunguCode
    );

    @Query("""
            SELECT COUNT(p)
            FROM Policy p
            WHERE p.applyEndDate IS NOT NULL
              AND p.applyEndDate >= :today
              AND p.recruitmentStatus <> :closedStatus
              AND (p.national = true OR EXISTS (
                    SELECT policyRegion.id
                    FROM PolicyRegion policyRegion
                    WHERE policyRegion.policy = p
                      AND policyRegion.sidoCode = :sidoCode
                      AND (policyRegion.regionCode IS NULL
                           OR policyRegion.regionCode.sigunguCode = :sigunguCode)
              ))
            """)
    long countVisibleUpcomingDeadlinePolicies(
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("today") LocalDate today,
            @Param("sidoCode") SidoCode sidoCode,
            @Param("sigunguCode") String sigunguCode
    );

    @Query("""
            SELECT COUNT(p)
            FROM Policy p
            WHERE p.category = :category
              AND p.applyEndDate IS NOT NULL
              AND p.applyEndDate >= :today
              AND p.recruitmentStatus <> :closedStatus
              AND (p.national = true OR EXISTS (
                    SELECT policyRegion.id
                    FROM PolicyRegion policyRegion
                    WHERE policyRegion.policy = p
                      AND policyRegion.sidoCode = :sidoCode
                      AND (policyRegion.regionCode IS NULL
                           OR policyRegion.regionCode.sigunguCode = :sigunguCode)
              ))
            """)
    long countVisibleUpcomingDeadlinePoliciesByCategory(
            @Param("category") PolicyCategoryType category,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("today") LocalDate today,
            @Param("sidoCode") SidoCode sidoCode,
            @Param("sigunguCode") String sigunguCode
    );

    @Query("""
            SELECT p
            FROM Policy p
            WHERE p.applyEndDate IS NOT NULL
              AND p.applyEndDate >= :today
              AND p.recruitmentStatus <> :closedStatus
              AND (p.national = true OR EXISTS (
                    SELECT policyRegion.id
                    FROM PolicyRegion policyRegion
                    WHERE policyRegion.policy = p
                      AND policyRegion.sidoCode = :sidoCode
                      AND (policyRegion.regionCode IS NULL
                           OR policyRegion.regionCode.sigunguCode = :sigunguCode)
              ))
            ORDER BY p.applyEndDate ASC, p.id DESC
            """)
    List<Policy> findAllUpcomingDeadlinePolicies(
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("today") LocalDate today,
            @Param("sidoCode") SidoCode sidoCode,
            @Param("sigunguCode") String sigunguCode,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Policy p
            WHERE p.applyEndDate IS NOT NULL
              AND p.applyEndDate >= :today
              AND p.recruitmentStatus <> :closedStatus
              AND (p.national = true OR EXISTS (
                    SELECT policyRegion.id
                    FROM PolicyRegion policyRegion
                    WHERE policyRegion.policy = p
                      AND policyRegion.sidoCode = :sidoCode
                      AND (policyRegion.regionCode IS NULL
                           OR policyRegion.regionCode.sigunguCode = :sigunguCode)
              ))
              AND (p.applyEndDate > :applyEndDate
                   OR (p.applyEndDate = :applyEndDate AND p.id < :policyId))
            ORDER BY p.applyEndDate ASC, p.id DESC
            """)
    List<Policy> findAllUpcomingDeadlinePoliciesAfter(
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("today") LocalDate today,
            @Param("sidoCode") SidoCode sidoCode,
            @Param("sigunguCode") String sigunguCode,
            @Param("applyEndDate") LocalDate applyEndDate,
            @Param("policyId") Long policyId,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Policy p
            WHERE p.category = :category
              AND p.applyEndDate IS NOT NULL
              AND p.applyEndDate >= :today
              AND p.recruitmentStatus <> :closedStatus
              AND (p.national = true OR EXISTS (
                    SELECT policyRegion.id
                    FROM PolicyRegion policyRegion
                    WHERE policyRegion.policy = p
                      AND policyRegion.sidoCode = :sidoCode
                      AND (policyRegion.regionCode IS NULL
                           OR policyRegion.regionCode.sigunguCode = :sigunguCode)
              ))
            ORDER BY p.applyEndDate ASC, p.id DESC
            """)
    List<Policy> findUpcomingDeadlinePolicies(
            @Param("category") PolicyCategoryType category,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("today") LocalDate today,
            @Param("sidoCode") SidoCode sidoCode,
            @Param("sigunguCode") String sigunguCode,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Policy p
            WHERE p.category = :category
              AND p.applyEndDate IS NOT NULL
              AND p.applyEndDate >= :today
              AND p.recruitmentStatus <> :closedStatus
              AND (p.national = true OR EXISTS (
                    SELECT policyRegion.id
                    FROM PolicyRegion policyRegion
                    WHERE policyRegion.policy = p
                      AND policyRegion.sidoCode = :sidoCode
                      AND (policyRegion.regionCode IS NULL
                           OR policyRegion.regionCode.sigunguCode = :sigunguCode)
              ))
              AND (p.applyEndDate > :applyEndDate
                   OR (p.applyEndDate = :applyEndDate AND p.id < :policyId))
            ORDER BY p.applyEndDate ASC, p.id DESC
            """)
    List<Policy> findUpcomingDeadlinePoliciesAfter(
            @Param("category") PolicyCategoryType category,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("today") LocalDate today,
            @Param("sidoCode") SidoCode sidoCode,
            @Param("sigunguCode") String sigunguCode,
            @Param("applyEndDate") LocalDate applyEndDate,
            @Param("policyId") Long policyId,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Policy p
            WHERE p.recruitmentStatus <> :closedStatus
              AND (p.national = true OR EXISTS (
                    SELECT policyRegion.id
                    FROM PolicyRegion policyRegion
                    WHERE policyRegion.policy = p
                      AND policyRegion.sidoCode = :sidoCode
                      AND (policyRegion.regionCode IS NULL
                           OR policyRegion.regionCode.sigunguCode = :sigunguCode)
              ))
            ORDER BY p.registeredAt DESC, p.id DESC
            """)
    List<Policy> findAllLatestPolicies(
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("sidoCode") SidoCode sidoCode,
            @Param("sigunguCode") String sigunguCode,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Policy p
            WHERE p.recruitmentStatus <> :closedStatus
              AND (p.national = true OR EXISTS (
                    SELECT policyRegion.id
                    FROM PolicyRegion policyRegion
                    WHERE policyRegion.policy = p
                      AND policyRegion.sidoCode = :sidoCode
                      AND (policyRegion.regionCode IS NULL
                           OR policyRegion.regionCode.sigunguCode = :sigunguCode)
              ))
              AND (p.registeredAt < :registeredAt
                   OR (p.registeredAt = :registeredAt AND p.id < :policyId))
            ORDER BY p.registeredAt DESC, p.id DESC
            """)
    List<Policy> findAllLatestPoliciesAfter(
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("sidoCode") SidoCode sidoCode,
            @Param("sigunguCode") String sigunguCode,
            @Param("registeredAt") LocalDateTime registeredAt,
            @Param("policyId") Long policyId,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Policy p
            WHERE p.category = :category
              AND p.recruitmentStatus <> :closedStatus
              AND (p.national = true OR EXISTS (
                    SELECT policyRegion.id
                    FROM PolicyRegion policyRegion
                    WHERE policyRegion.policy = p
                      AND policyRegion.sidoCode = :sidoCode
                      AND (policyRegion.regionCode IS NULL
                           OR policyRegion.regionCode.sigunguCode = :sigunguCode)
              ))
            ORDER BY p.registeredAt DESC, p.id DESC
            """)
    List<Policy> findLatestPolicies(
            @Param("category") PolicyCategoryType category,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("sidoCode") SidoCode sidoCode,
            @Param("sigunguCode") String sigunguCode,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Policy p
            WHERE p.category = :category
              AND p.recruitmentStatus <> :closedStatus
              AND (p.national = true OR EXISTS (
                    SELECT policyRegion.id
                    FROM PolicyRegion policyRegion
                    WHERE policyRegion.policy = p
                      AND policyRegion.sidoCode = :sidoCode
                      AND (policyRegion.regionCode IS NULL
                           OR policyRegion.regionCode.sigunguCode = :sigunguCode)
              ))
              AND (p.registeredAt < :registeredAt
                   OR (p.registeredAt = :registeredAt AND p.id < :policyId))
            ORDER BY p.registeredAt DESC, p.id DESC
            """)
    List<Policy> findLatestPoliciesAfter(
            @Param("category") PolicyCategoryType category,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("sidoCode") SidoCode sidoCode,
            @Param("sigunguCode") String sigunguCode,
            @Param("registeredAt") LocalDateTime registeredAt,
            @Param("policyId") Long policyId,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Policy p
            WHERE p.category = :category
              AND p.recruitmentStatus <> :closedStatus
              AND (p.national = true OR EXISTS (
                    SELECT policyRegion.id
                    FROM PolicyRegion policyRegion
                    WHERE policyRegion.policy = p
                      AND policyRegion.sidoCode = :sidoCode
                      AND (policyRegion.regionCode IS NULL
                           OR policyRegion.regionCode.sigunguCode = :sigunguCode)
              ))
            ORDER BY CASE WHEN p.applyEndDate IS NULL THEN 1 ELSE 0 END,
                     p.applyEndDate ASC,
                     p.id DESC
            """)
    List<Policy> findDeadlinePolicies(
            @Param("category") PolicyCategoryType category,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("sidoCode") SidoCode sidoCode,
            @Param("sigunguCode") String sigunguCode,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Policy p
            WHERE p.category = :category
              AND p.recruitmentStatus <> :closedStatus
              AND (p.national = true OR EXISTS (
                    SELECT policyRegion.id
                    FROM PolicyRegion policyRegion
                    WHERE policyRegion.policy = p
                      AND policyRegion.sidoCode = :sidoCode
                      AND (policyRegion.regionCode IS NULL
                           OR policyRegion.regionCode.sigunguCode = :sigunguCode)
              ))
              AND (p.applyEndDate > :applyEndDate
                   OR (p.applyEndDate = :applyEndDate AND p.id < :policyId)
                   OR p.applyEndDate IS NULL)
            ORDER BY CASE WHEN p.applyEndDate IS NULL THEN 1 ELSE 0 END,
                     p.applyEndDate ASC,
                     p.id DESC
            """)
    List<Policy> findDeadlinePoliciesAfterDatedCursor(
            @Param("category") PolicyCategoryType category,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("sidoCode") SidoCode sidoCode,
            @Param("sigunguCode") String sigunguCode,
            @Param("applyEndDate") LocalDate applyEndDate,
            @Param("policyId") Long policyId,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Policy p
            WHERE p.category = :category
              AND p.recruitmentStatus <> :closedStatus
              AND (p.national = true OR EXISTS (
                    SELECT policyRegion.id
                    FROM PolicyRegion policyRegion
                    WHERE policyRegion.policy = p
                      AND policyRegion.sidoCode = :sidoCode
                      AND (policyRegion.regionCode IS NULL
                           OR policyRegion.regionCode.sigunguCode = :sigunguCode)
              ))
              AND p.applyEndDate IS NULL
              AND p.id < :policyId
            ORDER BY p.id DESC
            """)
    List<Policy> findDeadlinePoliciesAfterOpenEndedCursor(
            @Param("category") PolicyCategoryType category,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("sidoCode") SidoCode sidoCode,
            @Param("sigunguCode") String sigunguCode,
            @Param("policyId") Long policyId,
            Pageable pageable
    );
}
