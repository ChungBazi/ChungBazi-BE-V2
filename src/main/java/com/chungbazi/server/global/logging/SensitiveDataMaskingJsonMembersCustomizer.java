package com.chungbazi.server.global.logging;

import java.util.regex.Pattern;
import org.springframework.boot.json.JsonWriter;
import org.springframework.boot.logging.structured.StructuredLoggingJsonMembersCustomizer;

public final class SensitiveDataMaskingJsonMembersCustomizer
        implements StructuredLoggingJsonMembersCustomizer<Object> {

    private static final String MASK = "***";

    private static final Pattern BEARER_TOKEN = Pattern.compile(
            "(?i)(\\bBearer\\s+)[A-Za-z0-9._~+/=-]+"
    );

    private static final Pattern SENSITIVE_KEY_VALUE = Pattern.compile(
            "(?i)(\\b(?:password|passwd|secret|access[_-]?token|refresh[_-]?token|"
                    + "authorization|api[_-]?key|fcm[_-]?token)\\b\\\"?\\s*[:=]\\s*)"
                    + "(\\\"[^\\\"]*\\\"|'[^']*'|(?:Bearer\\s+)?[^\\s,;&}]+)"
    );

    @Override
    public void customize(JsonWriter.Members<Object> members) {
        members.applyingValueProcessor(JsonWriter.ValueProcessor
                .of(String.class, SensitiveDataMaskingJsonMembersCustomizer::mask));
    }

    static String mask(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        String masked = SENSITIVE_KEY_VALUE.matcher(value).replaceAll("$1" + MASK);
        return BEARER_TOKEN.matcher(masked).replaceAll("$1" + MASK);
    }
}
