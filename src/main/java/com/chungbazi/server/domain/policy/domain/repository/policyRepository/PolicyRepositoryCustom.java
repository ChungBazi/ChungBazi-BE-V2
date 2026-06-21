package com.chungbazi.server.domain.policy.domain.repository.policyRepository;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.policy.domain.type.SidoCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface PolicyRepositoryCustom {

    long countVisiblePoliciesByCategory(
            PolicyCategoryType category,
            RecruitmentStatus closedStatus,
            SidoCode sidoCode,
            String sigunguCode
    );

    long countVisiblePolicies(
            RecruitmentStatus closedStatus,
            SidoCode sidoCode,
            String sigunguCode
    );

    long countVisibleUpcomingDeadlinePolicies(
            RecruitmentStatus closedStatus,
            LocalDate today,
            SidoCode sidoCode,
            String sigunguCode
    );

    long countVisibleUpcomingDeadlinePoliciesByCategory(
            PolicyCategoryType category,
            RecruitmentStatus closedStatus,
            LocalDate today,
            SidoCode sidoCode,
            String sigunguCode
    );

    List<Policy> findAllUpcomingDeadlinePolicies(
            RecruitmentStatus closedStatus,
            LocalDate today,
            SidoCode sidoCode,
            String sigunguCode,
            Pageable pageable
    );

    List<Policy> findAllUpcomingDeadlinePoliciesAfter(
            RecruitmentStatus closedStatus,
            LocalDate today,
            SidoCode sidoCode,
            String sigunguCode,
            LocalDate applyEndDate,
            Long policyId,
            Pageable pageable
    );

    List<Policy> findUpcomingDeadlinePolicies(
            PolicyCategoryType category,
            RecruitmentStatus closedStatus,
            LocalDate today,
            SidoCode sidoCode,
            String sigunguCode,
            Pageable pageable
    );

    List<Policy> findUpcomingDeadlinePoliciesAfter(
            PolicyCategoryType category,
            RecruitmentStatus closedStatus,
            LocalDate today,
            SidoCode sidoCode,
            String sigunguCode,
            LocalDate applyEndDate,
            Long policyId,
            Pageable pageable
    );

    List<Policy> findAllLatestPolicies(
            RecruitmentStatus closedStatus,
            SidoCode sidoCode,
            String sigunguCode,
            Pageable pageable
    );

    List<Policy> findAllLatestPoliciesAfter(
            RecruitmentStatus closedStatus,
            SidoCode sidoCode,
            String sigunguCode,
            LocalDateTime registeredAt,
            Long policyId,
            Pageable pageable
    );

    List<Policy> findLatestPolicies(
            PolicyCategoryType category,
            RecruitmentStatus closedStatus,
            SidoCode sidoCode,
            String sigunguCode,
            Pageable pageable
    );

    List<Policy> findLatestPoliciesAfter(
            PolicyCategoryType category,
            RecruitmentStatus closedStatus,
            SidoCode sidoCode,
            String sigunguCode,
            LocalDateTime registeredAt,
            Long policyId,
            Pageable pageable
    );

    List<Policy> findDeadlinePolicies(
            PolicyCategoryType category,
            RecruitmentStatus closedStatus,
            SidoCode sidoCode,
            String sigunguCode,
            Pageable pageable
    );

    List<Policy> findDeadlinePoliciesAfterDatedCursor(
            PolicyCategoryType category,
            RecruitmentStatus closedStatus,
            SidoCode sidoCode,
            String sigunguCode,
            LocalDate applyEndDate,
            Long policyId,
            Pageable pageable
    );

    List<Policy> findDeadlinePoliciesAfterOpenEndedCursor(
            PolicyCategoryType category,
            RecruitmentStatus closedStatus,
            SidoCode sidoCode,
            String sigunguCode,
            Long policyId,
            Pageable pageable
    );
}
