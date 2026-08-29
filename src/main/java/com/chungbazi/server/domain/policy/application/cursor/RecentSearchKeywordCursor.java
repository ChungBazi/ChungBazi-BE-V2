package com.chungbazi.server.domain.policy.application.cursor;

import java.time.LocalDateTime;

public record RecentSearchKeywordCursor(
        LocalDateTime lastSearchedAt,
        Long keywordId
) {
}
