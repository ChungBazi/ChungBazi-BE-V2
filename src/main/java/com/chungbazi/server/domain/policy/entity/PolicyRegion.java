package com.chungbazi.server.domain.policy.entity;

import com.chungbazi.server.domain.policy.enums.SidoCode;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "policy_region",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_policy_region_policy_sigungu",
                columnNames = {"policy_id", "sigungu_code"}
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

    @Enumerated(EnumType.STRING)
    @Column(name = "sido_code", nullable = false, length = 30)
    private SidoCode sidoCode;

    @Column(name = "sido_name", nullable = false, length = 50)
    private String sidoName;

    @Column(name = "sigungu_code", nullable = false, length = 10)
    private String sigunguCode;

    @Column(name = "sigungu_name", nullable = false, length = 50)
    private String sigunguName;

    public static PolicyRegion createPolicyRegion(
            Policy policy,
            SidoCode sidoCode,
            String sigunguCode,
            String sigunguName
    ) {
        SidoCode derivedSidoCode = SidoCode.fromSigunguCode(sigunguCode);
        if (sidoCode == null || sidoCode != derivedSidoCode) {
            throw new IllegalArgumentException("시도 코드와 시군구 코드가 일치하지 않습니다.");
        }

        PolicyRegion policyRegion = new PolicyRegion();
        policyRegion.policy = policy;
        policyRegion.sidoCode = sidoCode;
        policyRegion.sidoName = sidoCode.getName();
        policyRegion.sigunguCode = sigunguCode;
        policyRegion.sigunguName = sigunguName;
        return policyRegion;
    }
}
