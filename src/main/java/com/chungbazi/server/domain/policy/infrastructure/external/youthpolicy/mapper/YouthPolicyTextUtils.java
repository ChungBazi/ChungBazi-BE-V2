package com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.mapper;

final class YouthPolicyTextUtils {

    private YouthPolicyTextUtils() {
    }

    static boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }

    static String trimToNull(String value) {
        if (isBlank(value)) {
            return null;
        }
        return value.trim();
    }

    static String joinNonBlank(String delimiter, String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            String trimmed = trimToNull(value);
            if (trimmed == null) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(delimiter);
            }
            builder.append(trimmed);
        }
        return builder.isEmpty() ? null : builder.toString();
    }

    static Integer parseInteger(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }

        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
