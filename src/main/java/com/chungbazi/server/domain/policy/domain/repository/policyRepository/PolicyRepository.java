package com.chungbazi.server.domain.policy.domain.repository.policyRepository;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.type.PolicyDisplayStatus;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolicyRepository extends JpaRepository<Policy, Long>, PolicyRepositoryCustom {
    Optional<Policy> findByPlcyNo(String plcyNo);

    boolean existsByPlcyNo(String plcyNo);

    @Query("""
            SELECT policy
            FROM Policy policy
            WHERE policy.displayStatus = com.chungbazi.server.domain.policy.domain.type.PolicyDisplayStatus.VISIBLE
              AND policy.recruitmentStatus <> :closedStatus
            """)
    List<Policy> findAllSearchablePolicies(
            @Param("closedStatus") RecruitmentStatus closedStatus
    );

    @Query("""
            SELECT policy
            FROM Policy policy
            WHERE policy.applyEndDate IN :applyEndDates
              AND policy.recruitmentStatus <> :closedStatus
              AND policy.displayStatus = com.chungbazi.server.domain.policy.domain.type.PolicyDisplayStatus.VISIBLE
            """)
    List<Policy> findVisiblePoliciesByApplyEndDateIn(
            @Param("applyEndDates") Collection<LocalDate> applyEndDates,
            @Param("closedStatus") RecruitmentStatus closedStatus
    );

    @Query("""
            SELECT policy
            FROM Policy policy
            WHERE policy.id IN :policyIds
              AND policy.recruitmentStatus <> :closedStatus
              AND policy.displayStatus = com.chungbazi.server.domain.policy.domain.type.PolicyDisplayStatus.VISIBLE
            """)
    List<Policy> findVisiblePoliciesByIdIn(
            @Param("policyIds") Collection<Long> policyIds,
            @Param("closedStatus") RecruitmentStatus closedStatus
    );

    @Modifying
    @Query("""
            UPDATE Policy policy
            SET policy.viewCount = policy.viewCount + 1
            WHERE policy.id = :policyId
              AND policy.displayStatus = com.chungbazi.server.domain.policy.domain.type.PolicyDisplayStatus.VISIBLE
            """)
    int increaseViewCount(@Param("policyId") Long policyId);

    @Modifying
    @Query("""
            UPDATE Policy policy
            SET policy.saveCount = policy.saveCount + 1
            WHERE policy.id = :policyId
            """)
    int increaseSaveCount(@Param("policyId") Long policyId);

    @Modifying
    @Query("""
            UPDATE Policy policy
            SET policy.saveCount = policy.saveCount - 1
            WHERE policy.id = :policyId
              AND policy.saveCount > 0
            """)
    int decreaseSaveCount(@Param("policyId") Long policyId);

    @Modifying
    @Query("""
            UPDATE Policy policy
            SET policy.displayStatus = :hiddenStatus
            WHERE policy.displayStatus = :visibleStatus
              AND policy.recruitmentStatus = :closedStatus
              AND policy.applyEndDate <= :thresholdDate
            """)
    int hideExpiredPolicies(
            @Param("visibleStatus") PolicyDisplayStatus visibleStatus,
            @Param("hiddenStatus") PolicyDisplayStatus hiddenStatus,
            @Param("closedStatus") RecruitmentStatus closedStatus,
            @Param("thresholdDate") LocalDate thresholdDate
    );
}
