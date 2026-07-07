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

    public static String encode(PolicySortType sort, Policy policy) {
        String sortValue = sort.name();
        String dateValue = sort == PolicySortType.LATEST
                ? policy.getRegisteredAt().toString()
                : policy.getApplyEndDate() == null
                ? NULL_DATE
                : policy.getApplyEndDate().toString();

        String rawCursor = String.join(
                CURSOR_JOINER,
                sortValue,
                dateValue,
                policy.getId().toString()
        );

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
            if (values.length != 3 || !requestedSort.name().equals(values[0])) {
                throw new IllegalArgumentException();
            }

            Long policyId = Long.valueOf(values[2]);

            if (requestedSort == PolicySortType.LATEST) {
                return new PolicyCursor(LocalDateTime.parse(values[1]), null, policyId);
            }

            LocalDate applyEndDate = NULL_DATE.equals(values[1])
                    ? null
                    : LocalDate.parse(values[1]);

            return new PolicyCursor(null, applyEndDate, policyId);
        } catch (RuntimeException exception) {
            throw new PolicyException(PolicyErrorCode.INVALID_POLICY_CURSOR);
        }
    }
}
