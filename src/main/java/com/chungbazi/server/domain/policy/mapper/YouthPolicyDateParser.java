package com.chungbazi.server.domain.policy.mapper;

import com.chungbazi.server.domain.policy.dto.internal.DateRange;
import com.chungbazi.server.domain.policy.dto.internal.ParsedPeriod;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class YouthPolicyDateParser {

    private static final DateTimeFormatter BASIC_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter SOURCE_DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DATE_TOKEN =
            "(\\d{8}|\\d{4}\\s*[./-]\\s*\\d{1,2}\\s*[./-]\\s*\\d{1,2}"
                    + "|\\d{4}\\s*년\\s*\\d{1,2}\\s*월\\s*\\d{1,2}\\s*일?)";
    private static final Pattern DATE_RANGE_PATTERN = Pattern.compile(
            DATE_TOKEN + "\\s*(?:~|부터)\\s*" + DATE_TOKEN
    );
    private static final Pattern YEAR_MONTH_RANGE_PATTERN = Pattern.compile(
            "(\\d{4})\\s*\\.\\s*(\\d{1,2})\\s*\\.?\\s*~\\s*"
                    + "(?:(\\d{4})\\s*\\.\\s*)?(\\d{1,2})\\s*\\.?"
    );
    private static final Pattern YEAR_PATTERN = Pattern.compile("(?<!\\d)((?:19|20)\\d{2})(?!\\d)");
    private static final Pattern SEPARATED_DATE_PATTERN =
            Pattern.compile("(\\d{4})\\D+(\\d{1,2})\\D+(\\d{1,2})");

    public ParsedPeriod findAlwaysOpenPeriod(
            String businessStartDate,
            String businessEndDate,
            String businessPeriodText,
            String registeredAt
    ) {
        String startText = YouthPolicyTextUtils.trimToNull(businessStartDate);
        String endText = YouthPolicyTextUtils.trimToNull(businessEndDate);
        String etcText = YouthPolicyTextUtils.trimToNull(businessPeriodText);

        //사업기간 날짜로 파싱 후 모집 날짜로 저장
        DateRange separateDateRange = parseSeparateDates(startText, endText);
        if (separateDateRange != null) {
            return new ParsedPeriod(separateDateRange, startText + " ~ " + endText);
        }

        //사업관련 기타 내용에 기간이 적혀있는 경우, 모집 날짜 파싱
        DateRange etcDateRange = parseDateRange(etcText);
        if (etcDateRange != null) {
            return new ParsedPeriod(etcDateRange, etcText);
        }

        //연중 모집 정책 모집날짜 파싱
        return parseAnnualPeriod(etcText, startText, registeredAt);
    }

    public DateRange parseDateRange(String value) {
        String normalized = YouthPolicyTextUtils.trimToNull(value);
        if (normalized == null) {
            return null;
        }

        Matcher matcher = DATE_RANGE_PATTERN.matcher(normalized);
        if (matcher.find()) {
            return createDateRange(parseDate(matcher.group(1)), parseDate(matcher.group(2)));
        }
        return parseYearMonthRange(normalized);
    }

    public LocalDateTime parseRegisteredAtOrNow(String value) {
        String normalized = YouthPolicyTextUtils.trimToNull(value);
        if (normalized == null) {
            return LocalDateTime.now();
        }

        try {
            return LocalDateTime.parse(normalized, SOURCE_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.now();
        }
    }

    private ParsedPeriod parseAnnualPeriod(
            String businessPeriodText,
            String businessStartDate,
            String registeredAt
    ) {
        if (!containsAnnualText(businessPeriodText)) {
            return null;
        }

        Integer year = findReferenceYear(businessPeriodText, businessStartDate, registeredAt);
        if (year == null) {
            return null;
        }

        DateRange dateRange = new DateRange(
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 31)
        );
        return new ParsedPeriod(dateRange, businessPeriodText);
    }

    private DateRange parseSeparateDates(String startValue, String endValue) {
        return createDateRange(parseDate(startValue), parseDate(endValue));
    }

    private DateRange parseYearMonthRange(String value) {
        Matcher matcher = YEAR_MONTH_RANGE_PATTERN.matcher(value);
        if (!matcher.find()) {
            return null;
        }

        int startYear = Integer.parseInt(matcher.group(1));
        int startMonth = Integer.parseInt(matcher.group(2));
        int endYear = matcher.group(3) == null
                ? startYear
                : Integer.parseInt(matcher.group(3));
        int endMonth = Integer.parseInt(matcher.group(4));

        try {
            LocalDate startDate = YearMonth.of(startYear, startMonth).atDay(1);
            LocalDate endDate = YearMonth.of(endYear, endMonth).atEndOfMonth();
            return createDateRange(startDate, endDate);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private DateRange createDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return null;
        }
        return new DateRange(startDate, endDate);
    }

    private LocalDate parseDate(String value) {
        String normalized = YouthPolicyTextUtils.trimToNull(value);
        if (normalized == null) {
            return null;
        }

        String digits = normalized.replaceAll("\\D", "");
        if (digits.length() == 8) {
            try {
                return LocalDate.parse(digits, BASIC_DATE_FORMATTER);
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }

        Matcher matcher = SEPARATED_DATE_PATTERN.matcher(normalized);
        if (!matcher.find()) {
            return null;
        }
        try {
            return LocalDate.of(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3))
            );
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private Integer findReferenceYear(String periodText, String startDate, String registeredAt) {
        Integer textYear = extractYear(periodText);
        if (textYear != null) {
            return textYear;
        }

        LocalDate parsedStartDate = parseDate(startDate);
        if (parsedStartDate != null) {
            return parsedStartDate.getYear();
        }
        return extractYear(registeredAt);
    }

    private Integer extractYear(String value) {
        String normalized = YouthPolicyTextUtils.trimToNull(value);
        if (normalized == null) {
            return null;
        }
        Matcher matcher = YEAR_PATTERN.matcher(normalized);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    private boolean containsAnnualText(String value) {
        String normalized = YouthPolicyTextUtils.trimToNull(value);
        return normalized != null
                && (normalized.contains("연중")
                || normalized.contains("연간")
                || normalized.contains("1년 내내"));
    }
}
