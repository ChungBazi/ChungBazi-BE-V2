package com.chungbazi.server.domain.policy.application.cursor;

import com.chungbazi.server.domain.policy.domain.entity.RecentViewedPolicy;
import com.chungbazi.server.domain.policy.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.exception.PolicyException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

public final class RecentViewedPolicyCursorParser {

    private static final String CURSOR_SEPARATOR = "\\|";
    private static final String CURSOR_JOINER = "|";
    private static final String CURSOR_PREFIX = "RECENT_VIEWED";

    private RecentViewedPolicyCursorParser() {
    }

    public static String encode(RecentViewedPolicy recentViewedPolicy) {
        String rawCursor = String.join(
                CURSOR_JOINER,
                CURSOR_PREFIX,
                recentViewedPolicy.getViewedAt().toString(),
                recentViewedPolicy.getPolicy().getId().toString()
        );

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(rawCursor.getBytes(StandardCharsets.UTF_8));
    }

    public static RecentViewedPolicyCursor decode(String encodedCursor) {
        if (encodedCursor == null || encodedCursor.isBlank()) {
            return null;
        }

        try {
            String rawCursor = new String(
                    Base64.getUrlDecoder().decode(encodedCursor),
                    StandardCharsets.UTF_8
            );
            String[] values = rawCursor.split(CURSOR_SEPARATOR, -1);

            if (values.length != 3 || !CURSOR_PREFIX.equals(values[0])) {
                throw new PolicyException(PolicyErrorCode.INVALID_POLICY_CURSOR);
            }

            return new RecentViewedPolicyCursor(
                    LocalDateTime.parse(values[1]),
                    Long.valueOf(values[2])
            );
        } catch (RuntimeException exception) {
            throw new PolicyException(PolicyErrorCode.INVALID_POLICY_CURSOR);
        }
    }
}
