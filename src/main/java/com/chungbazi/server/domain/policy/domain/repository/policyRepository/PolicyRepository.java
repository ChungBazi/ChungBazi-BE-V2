package com.chungbazi.server.domain.policy.domain.repository.policyRepository;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
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

    List<Policy> findAllByApplyEndDateInAndRecruitmentStatusNot(
            Collection<LocalDate> applyEndDates,
            RecruitmentStatus recruitmentStatus
    );

    List<Policy> findAllByIdInAndRecruitmentStatusNot(
            Collection<Long> policyIds,
            RecruitmentStatus recruitmentStatus
    );

    @Modifying
    @Query("""
            UPDATE Policy policy
            SET policy.viewCount = policy.viewCount + 1
            WHERE policy.id = :policyId
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
}
