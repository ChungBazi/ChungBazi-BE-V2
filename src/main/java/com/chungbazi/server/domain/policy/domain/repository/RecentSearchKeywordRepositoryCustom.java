package com.chungbazi.server.domain.policy.domain.repository;

import com.chungbazi.server.domain.policy.domain.entity.RecentSearchKeyword;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface RecentSearchKeywordRepositoryCustom {

    List<RecentSearchKeyword> findRecentSearchKeywords(
            Long userId,
            LocalDateTime lastSearchedAt,
            Long keywordId,
            Pageable pageable
    );
}
