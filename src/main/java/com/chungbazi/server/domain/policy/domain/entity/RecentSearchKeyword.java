package com.chungbazi.server.domain.policy.domain.entity;

import com.chungbazi.server.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "recent_search_keyword",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_recent_search_keyword_user_keyword",
                        columnNames = {"user_id", "keyword"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_recent_search_keyword_user_last_searched",
                        columnList = "user_id, last_searched_at"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecentSearchKeyword extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recent_search_keyword_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Column(name = "last_searched_at", nullable = false)
    private LocalDateTime lastSearchedAt;

    public static RecentSearchKeyword create(Long userId, String keyword) {
        RecentSearchKeyword recentSearchKeyword = new RecentSearchKeyword();
        recentSearchKeyword.userId = userId;
        recentSearchKeyword.keyword = keyword;
        recentSearchKeyword.lastSearchedAt = LocalDateTime.now();
        return recentSearchKeyword;
    }

    public void refresh() {
        this.lastSearchedAt = LocalDateTime.now();
    }
}
