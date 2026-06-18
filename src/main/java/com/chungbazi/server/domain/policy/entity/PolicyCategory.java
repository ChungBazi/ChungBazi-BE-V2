package com.chungbazi.server.domain.policy.entity;

import com.chungbazi.server.domain.policy.enums.PolicyCategoryType;
import com.chungbazi.server.domain.policy.enums.PolicySubCategoryType;
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
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "policy_category",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_policy_category_policy_sub_category",
                columnNames = {"policy_id", "sub_category"}
        )
)
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

    @Enumerated(EnumType.STRING)
    @Column(name = "sub_category", nullable = false, length = 40)
    private PolicySubCategoryType subCategory;

    @Column(name = "weight", nullable = false, precision = 3, scale = 2)
    private BigDecimal weight;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    public static PolicyCategory createPolicyCategory(
            Policy policy,
            PolicySubCategoryType subCategory,
            BigDecimal weight,
            boolean primary
    ) {
        if (subCategory == null) {
            throw new IllegalArgumentException("중분류 카테고리는 필수입니다.");
        }

        BigDecimal normalizedWeight = weight == null ? BigDecimal.ONE : weight;
        if (normalizedWeight.compareTo(BigDecimal.ZERO) < 0
                || normalizedWeight.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("카테고리 가중치는 0 이상 1 이하여야 합니다.");
        }

        PolicyCategory policyCategory = new PolicyCategory();
        policyCategory.policy = policy;
        policyCategory.category = subCategory.getCategory();
        policyCategory.subCategory = subCategory;
        policyCategory.weight = normalizedWeight;
        policyCategory.primary = primary;

        return policyCategory;
    }
}
