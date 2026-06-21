package com.chungbazi.server.domain.policy.domain.entity;

import com.chungbazi.server.domain.policy.infrastructure.persistence.converter.SidoCodeConverter;
import com.chungbazi.server.domain.policy.domain.type.SidoCode;
import com.chungbazi.server.domain.policy.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.exception.PolicyException;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
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
        name = "policy_region",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_policy_region_scope",
                columnNames = {"policy_id", "sido_code", "sigungu_code"}
        ),
        indexes = @Index(
                name = "idx_policy_region_lookup",
                columnList = "sido_code, sigungu_code, policy_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PolicyRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_region_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Convert(converter = SidoCodeConverter.class)
    @Column(name = "sido_code", nullable = false, length = 2)
    private SidoCode sidoCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sigungu_code")
    private RegionCode regionCode;

    public static PolicyRegion createSidoPolicyRegion(Policy policy, SidoCode sidoCode) {
        if (policy == null || sidoCode == null) {
            throw new PolicyException(PolicyErrorCode.INVALID_POLICY_REGION);
        }

        PolicyRegion policyRegion = new PolicyRegion();
        policyRegion.policy = policy;
        policyRegion.sidoCode = sidoCode;
        return policyRegion;
    }

    public static PolicyRegion createSigunguPolicyRegion(Policy policy, RegionCode regionCode) {
        if (policy == null || regionCode == null) {
            throw new PolicyException(PolicyErrorCode.INVALID_POLICY_REGION);
        }

        PolicyRegion policyRegion = new PolicyRegion();
        policyRegion.policy = policy;
        policyRegion.sidoCode = regionCode.getSidoCode();
        policyRegion.regionCode = regionCode;
        return policyRegion;
    }
}
