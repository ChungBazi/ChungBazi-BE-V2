package com.chungbazi.server.domain.policy.domain.entity;

import com.chungbazi.server.domain.policy.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.exception.PolicyException;
import com.chungbazi.server.domain.user.domain.type.SpecialEligibilityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "policy_special_eligibility",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_policy_special_eligibility_policy_type",
                columnNames = {"policy_id", "eligibility_type"}
        ),
        indexes = @Index(
                name = "idx_policy_special_eligibility_lookup",
                columnList = "eligibility_type, policy_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PolicySpecialEligibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_special_eligibility_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Enumerated(EnumType.STRING)
    @Column(name = "eligibility_type", nullable = false, length = 40)
    private SpecialEligibilityType eligibilityType;

    public static PolicySpecialEligibility create(
            Policy policy,
            SpecialEligibilityType eligibilityType
    ) {
        if (policy == null || eligibilityType == null) {
            throw new PolicyException(PolicyErrorCode.INVALID_POLICY_SPECIAL_ELIGIBILITY);
        }

        PolicySpecialEligibility specialEligibility = new PolicySpecialEligibility();
        specialEligibility.policy = policy;
        specialEligibility.eligibilityType = eligibilityType;
        return specialEligibility;
    }
}
