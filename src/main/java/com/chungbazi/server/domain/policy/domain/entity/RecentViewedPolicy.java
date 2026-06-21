package com.chungbazi.server.domain.policy.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "recent_viewed_policy")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecentViewedPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recent_viewed_policy_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt = LocalDateTime.now();

    public static RecentViewedPolicy createRecentViewedPolicy(Long userId, Policy policy) {
        RecentViewedPolicy recentViewedPolicy = new RecentViewedPolicy();
        recentViewedPolicy.userId = userId;
        recentViewedPolicy.policy = policy;
        recentViewedPolicy.viewedAt = LocalDateTime.now();
        return recentViewedPolicy;
    }
}
