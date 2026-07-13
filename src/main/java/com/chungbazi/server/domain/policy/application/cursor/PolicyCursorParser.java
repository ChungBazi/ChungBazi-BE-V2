package com.chungbazi.server.domain.policy.application.cursor;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.type.PolicySortType;
import com.chungbazi.server.domain.policy.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.exception.PolicyException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;

public final class PolicyCursorParser {

    private static final String CURSOR_SEPARATOR = "\\|";
    private static final String CURSOR_JOINER = "|";
    private static final String NULL_DATE = "NULL";
    private static final long SAVE_COUNT_WEIGHT = 5L;

    private PolicyCursorParser() {
    }

    public static String encode(PolicySortType sort, Policy policy) {

        String rawCursor;

        if (sort == PolicySortType.POPULAR) {
            rawCursor = String.join(
                    CURSOR_JOINER,
                    sort.name(),
                    String.valueOf(calculatePopularityScore(policy)),
                    policy.getRegisteredAt().toString(),
                    policy.getId().toString()
            );
        } else {
            String dateValue = sort == PolicySortType.LATEST
                    ? policy.getRegisteredAt().toString()
                    : policy.getApplyEndDate() == null
                    ? NULL_DATE
                    : policy.getApplyEndDate().toString();

            rawCursor = String.join(
                    CURSOR_JOINER,
                    sort.name(),
                    dateValue,
                    policy.getId().toString()
            );
        }

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(rawCursor.getBytes(StandardCharsets.UTF_8));
    }

    public static PolicyCursor decode(String encodedCursor, PolicySortType requestedSort) {
        if (encodedCursor == null || encodedCursor.isBlank()) {
            return null;
        }

        try {
            String rawCursor = new String(
                    Base64.getUrlDecoder().decode(encodedCursor),
                    StandardCharsets.UTF_8
            );

            String[] values = rawCursor.split(CURSOR_SEPARATOR, -1);

            if (requestedSort == PolicySortType.POPULAR) {
                if (values.length != 4 || !requestedSort.name().equals(values[0])) {
                    throw new PolicyException(PolicyErrorCode.INVALID_POLICY_CURSOR);
                }

                Long popularityScore = Long.valueOf(values[1]);
                LocalDateTime registeredAt = LocalDateTime.parse(values[2]);
                Long policyId = Long.valueOf(values[3]);

                return new PolicyCursor(registeredAt, null, policyId, popularityScore);
            }

            if (values.length != 3 || !requestedSort.name().equals(values[0])) {
                throw new PolicyException(PolicyErrorCode.INVALID_POLICY_CURSOR);
            }

            Long policyId = Long.valueOf(values[2]);

            if (requestedSort == PolicySortType.LATEST) {
                return new PolicyCursor(LocalDateTime.parse(values[1]), null, policyId, null);
            }

            LocalDate applyEndDate = NULL_DATE.equals(values[1])
                    ? null
                    : LocalDate.parse(values[1]);

            return new PolicyCursor(null, applyEndDate, policyId, null);
        } catch (RuntimeException exception) {
            throw new PolicyException(PolicyErrorCode.INVALID_POLICY_CURSOR);
        }
    }

    private static long calculatePopularityScore(Policy policy) {
        return policy.getViewCount() + policy.getSaveCount() * SAVE_COUNT_WEIGHT;
    }
}
