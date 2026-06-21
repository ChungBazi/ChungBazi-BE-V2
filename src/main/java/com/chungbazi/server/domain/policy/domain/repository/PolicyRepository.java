package com.chungbazi.server.domain.policy.domain.repository;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolicyRepository extends JpaRepository<Policy, Long> {
    Optional<Policy> findByPlcyNo(String plcyNo);

    boolean existsByPlcyNo(String plcyNo);

    long countByCategoryAndRecruitmentStatusNot(
            PolicyCategoryType category,
            RecruitmentStatus recruitmentStatus
    );

    long countByRecruitmentStatusNot(RecruitmentStatus recruitmentStatus);

    @Query("""
            SELECT p
            FROM Policy p
            WHERE p.recruitmentStatus <> :closedStatus
            ORDER BY p.registeredAt DESC, p.id DESC
            """)
    List<Policy> findAllLatestPolicies(
            @Param("closedStatus") RecruitmentStatus closedStatus,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Policy p
            WHERE p.recruitmentStatus <> :closedStatus
              AND (p.registeredAt < :registeredAt
                   OR (p.registeredAt = :registeredAt AND p.id < :policyId))
            ORDER BY p.registeredAt DESC, p.id DESC
            """)
    List<Policy> findAllLatestPoliciesAfter(
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("registeredAt") LocalDateTime registeredAt,
            @Param("policyId") Long policyId,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Policy p
            WHERE p.category = :category
              AND p.recruitmentStatus <> :closedStatus
            ORDER BY p.registeredAt DESC, p.id DESC
            """)
    List<Policy> findLatestPolicies(
            @Param("category") PolicyCategoryType category,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Policy p
            WHERE p.category = :category
              AND p.recruitmentStatus <> :closedStatus
              AND (p.registeredAt < :registeredAt
                   OR (p.registeredAt = :registeredAt AND p.id < :policyId))
            ORDER BY p.registeredAt DESC, p.id DESC
            """)
    List<Policy> findLatestPoliciesAfter(
            @Param("category") PolicyCategoryType category,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("registeredAt") LocalDateTime registeredAt,
            @Param("policyId") Long policyId,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Policy p
            WHERE p.category = :category
              AND p.recruitmentStatus <> :closedStatus
            ORDER BY CASE WHEN p.applyEndDate IS NULL THEN 1 ELSE 0 END,
                     p.applyEndDate ASC,
                     p.id DESC
            """)
    List<Policy> findDeadlinePolicies(
            @Param("category") PolicyCategoryType category,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Policy p
            WHERE p.category = :category
              AND p.recruitmentStatus <> :closedStatus
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
            @Param("applyEndDate") LocalDate applyEndDate,
            @Param("policyId") Long policyId,
            Pageable pageable
    );

    @Query("""
            SELECT p
            FROM Policy p
            WHERE p.category = :category
              AND p.recruitmentStatus <> :closedStatus
              AND p.applyEndDate IS NULL
              AND p.id < :policyId
            ORDER BY p.id DESC
            """)
    List<Policy> findDeadlinePoliciesAfterOpenEndedCursor(
            @Param("category") PolicyCategoryType category,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("policyId") Long policyId,
            Pageable pageable
    );
}
