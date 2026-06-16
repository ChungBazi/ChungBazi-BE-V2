package com.chungbazi.server.domain.policy.entity;

import com.chungbazi.server.domain.policy.enums.PolicyCategoryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "policy_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PolicyCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_category_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private PolicyCategoryType category;

    @Column(name = "weight", nullable = false, precision = 3, scale = 2)
    private BigDecimal weight;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    public static PolicyCategory createPolicyCategory(
            Policy policy,
            PolicyCategoryType category,
            BigDecimal weight,
            boolean primary
    ) {
        PolicyCategory policyCategory = new PolicyCategory();
        policyCategory.policy = policy;
        policyCategory.category = category;
        policyCategory.weight = weight == null ? BigDecimal.ONE : weight;
        policyCategory.primary = primary;

        return policyCategory;
    }
}
