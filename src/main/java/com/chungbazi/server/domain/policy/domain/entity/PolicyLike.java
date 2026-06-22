package com.chungbazi.server.domain.policy.domain.entity;

import com.chungbazi.server.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
        name = "policy_like",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_policy_like_user_policy",
                        columnNames = {"user_id", "policy_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PolicyLike extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_like_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(name = "memo", columnDefinition = "text")
    private String memo;

    public static PolicyLike createPolicyLike(Long userId, Policy policy, String memo) {
        PolicyLike policyLike = new PolicyLike();
        policyLike.userId = userId;
        policyLike.policy = policy;
        policyLike.memo = memo;
        return policyLike;
    }
}
