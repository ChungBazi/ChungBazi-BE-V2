package com.chungbazi.server.domain.policy.application.cursor;

import com.chungbazi.server.domain.policy.domain.entity.RecentSearchKeyword;
import com.chungbazi.server.domain.policy.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.exception.PolicyException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

public final class RecentSearchKeywordCursorParser {

    private static final String CURSOR_SEPARATOR = "\\|";
    private static final String CURSOR_JOINER = "|";

    private RecentSearchKeywordCursorParser() {
    }

    public static String encode(RecentSearchKeyword recentSearchKeyword) {
        String rawCursor = String.join(
                CURSOR_JOINER,
                recentSearchKeyword.getLastSearchedAt().toString(),
                recentSearchKeyword.getId().toString()
        );

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(rawCursor.getBytes(StandardCharsets.UTF_8));
    }

    public static RecentSearchKeywordCursor decode(String encodedCursor) {
        if (encodedCursor == null || encodedCursor.isBlank()) {
            return null;
        }

        try {
            String rawCursor = new String(
                    Base64.getUrlDecoder().decode(encodedCursor),
                    StandardCharsets.UTF_8
            );

            String[] values = rawCursor.split(CURSOR_SEPARATOR, -1);
            if (values.length != 2) {
                throw new IllegalArgumentException();
            }

            return new RecentSearchKeywordCursor(
                    LocalDateTime.parse(values[0]),
                    Long.valueOf(values[1])
            );
        } catch (RuntimeException exception) {
            throw new PolicyException(PolicyErrorCode.INVALID_RECENT_SEARCH_KEYWORD_CURSOR);
        }
    }
}
