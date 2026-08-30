package com.chungbazi.server.domain.policy.application.mapper;

import com.chungbazi.server.domain.policy.api.dto.response.PolicyCardResponse;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyDetailResponse;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse.PolicySummary;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.PolicyDetail;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentType;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class PolicyDisplayMapper {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public PolicySummary toSummary(Policy policy, Set<Long> likedPolicyIds) {
        return new PolicySummary(
                policy.getId(),
                policy.getCategory(),
                policy.getCategory().getDescription(),
                formatDDay(policy),
                policy.getTitle(),
                policy.getViewCount(),
                likedPolicyIds.contains(policy.getId())
        );
    }

    public List<PolicySummary> toSummaries(List<Policy> policies, Set<Long> likedPolicyIds) {
        return policies.stream()
                .map(policy -> toSummary(policy, likedPolicyIds))
                .toList();
    }

    public PolicyCardResponse toCardResponse(Policy policy, Set<Long> likedPolicyIds) {
        return new PolicyCardResponse(
                policy.getId(),
                policy.getCategory(),
                policy.getCategory().getDescription(),
                formatDDay(policy),
                policy.getTitle(),
                formatApplyPeriod(policy),
                policy.getSummary(),
                policy.getSupportContent(),
                policy.getApplyUrl(),
                likedPolicyIds.contains(policy.getId())
        );
    }

    public PolicyDetailResponse toDetailResponse(
            Policy policy,
            PolicyDetail policyDetail,
            Set<Long> likedPolicyIds,
            List<Policy> personalizedPolicies,
            List<Policy> popularPolicies
    ) {
        return new PolicyDetailResponse(
                policy.getId(),
                policy.getCategory(),
                policy.getCategory().getDescription(),
                formatDDay(policy),
                policy.getTitle(),
                policy.getSummary(),
                policy.getViewCount(),
                likedPolicyIds.contains(policy.getId()),
                policyDetail == null ? null : policyDetail.getEligibilityDescription(),
                formatApplyPeriod(policy),
                policy.getSupportContent(),
                policy.getApplyUrl(),
                policyDetail == null ? null : policyDetail.getApplicationMethod(),
                policyDetail == null ? null : policyDetail.getSubmittedDocument(),
                policyDetail == null ? null : policyDetail.getScreeningMethod(),
                toReferenceUrls(policyDetail),
                toDetailSummaries(personalizedPolicies, likedPolicyIds),
                toDetailSummaries(popularPolicies, likedPolicyIds)
        );
    }

    private List<String> toReferenceUrls(PolicyDetail policyDetail) {
        if (policyDetail == null) {
            return List.of();
        }

        List<String> referenceUrls = new ArrayList<>();
        if (policyDetail.getReferenceUrl1() != null && !policyDetail.getReferenceUrl1().isBlank()) {
            referenceUrls.add(policyDetail.getReferenceUrl1());
        }
        if (policyDetail.getReferenceUrl2() != null && !policyDetail.getReferenceUrl2().isBlank()) {
            referenceUrls.add(policyDetail.getReferenceUrl2());
        }
        return referenceUrls;
    }

    private List<PolicyDetailResponse.PolicySummary> toDetailSummaries(
            List<Policy> policies,
            Set<Long> likedPolicyIds
    ) {
        return policies.stream()
                .map(policy -> new PolicyDetailResponse.PolicySummary(
                        policy.getId(),
                        policy.getCategory(),
                        policy.getCategory().getDescription(),
                        formatDDay(policy),
                        policy.getTitle(),
                        policy.getViewCount(),
                        likedPolicyIds.contains(policy.getId())
                ))
                .toList();
    }

    private String formatDDay(Policy policy) {
        if (policy.getRecruitmentType() == RecruitmentType.ALWAYS) {
            return "상시";
        }
        if (policy.getApplyEndDate() == null) {
            return "미정";
        }

        long remainingDays = ChronoUnit.DAYS.between(
                LocalDate.now(SERVICE_ZONE_ID),
                policy.getApplyEndDate()
        );
        if (remainingDays < 0) {
            return "마감";
        }
        if (remainingDays == 0) {
            return "D-Day";
        }
        return "D-" + remainingDays;
    }

    private String formatApplyPeriod(Policy policy) {
        if (policy.getApplyStartDate() != null && policy.getApplyEndDate() != null) {
            return policy.getApplyStartDate().format(DATE_FORMATTER)
                    + " - "
                    + policy.getApplyEndDate().format(DATE_FORMATTER);
        }
        if (policy.getApplyPeriodText() != null && !policy.getApplyPeriodText().isBlank()) {
            return policy.getApplyPeriodText();
        }
        if (policy.getRecruitmentType() == RecruitmentType.ALWAYS) {
            return "상시 모집";
        }
        return "미정";
    }
}
