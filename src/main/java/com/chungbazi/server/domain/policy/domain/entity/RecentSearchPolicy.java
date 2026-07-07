package com.chungbazi.server.domain.policy.domain.entity;

import com.chungbazi.server.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "recent_search_policy",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_recent_search_policy_user_keyword",
                        columnNames = {"user_id", "keyword"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_recent_search_policy_user_updated",
                        columnList = "user_id, updated_at"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecentSearchPolicy extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recent_search_keyword_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "keyword", nullable = false, length = 50)
    private String keyword;

    @Column(name = "last_searched_at", nullable = false)
    private LocalDateTime lastSearchedAt;

    public static RecentSearchPolicy create(Long userId, String keyword) {
        RecentSearchPolicy recentSearchPolicy = new RecentSearchPolicy();
        recentSearchPolicy.userId = userId;
        recentSearchPolicy.keyword = keyword;
        recentSearchPolicy.lastSearchedAt = LocalDateTime.now();
        return recentSearchPolicy;
    }

    public void refresh() {
        this.lastSearchedAt = LocalDateTime.now();
    }
}
