package com.chungbazi.server.domain.policy.mapper;

import com.chungbazi.server.domain.policy.client.dto.YouthPolicyItem;
import com.chungbazi.server.domain.policy.enums.RecruitmentStatus;
import com.chungbazi.server.domain.policy.enums.RecruitmentType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class YouthPolicyDateMapper {

    static final String FIXED_PERIOD_CODE = "0057001";
    static final String ALWAYS_OPEN_CODE = "0057002";
    static final String CLOSED_CODE = "0057003";

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

    public ApplyPeriod toApplyPeriod(YouthPolicyItem item) {
        return toApplyPeriod(
                item.aplyPrdSeCd(),
                item.aplyYmd(),
                item.bizPrdBgngYmd(),
                item.bizPrdEndYmd(),
                item.bizPrdEtcCn(),
                item.frstRegDt()
        );
    }

    ApplyPeriod toApplyPeriod(
            String applyPeriodCode,
            String applyPeriodText,
            String businessStartDate,
            String businessEndDate,
            String businessPeriodText,
            String registeredAt
    ) {
        String normalizedCode = YouthPolicyTextUtils.trimToNull(applyPeriodCode);

        //특정 기간에 모집하는 정책일 경우
        if (FIXED_PERIOD_CODE.equals(normalizedCode)) {
            return mapFixedPeriod(applyPeriodText);
        }

        //온통청년 사이트에서 상시모집 정책으로 간주하는 경우
        if (ALWAYS_OPEN_CODE.equals(normalizedCode)) {
            //모집관련 필드를 파싱해 정책 날짜를 구분
            return mapAlwaysOpenPeriod(
                    businessStartDate,
                    businessEndDate,
                    businessPeriodText,
                    registeredAt
            );
        }

        //마감된 정책일 경우
        if (CLOSED_CODE.equals(normalizedCode)) {
            return new ApplyPeriod(null, null, null, null, RecruitmentStatus.CLOSED);
        }


        return mapUnknownPeriod(applyPeriodText);
    }

    public LocalDateTime toRegisteredAt(String frstRegDt) {
        String trimmed = YouthPolicyTextUtils.trimToNull(frstRegDt);
        if (trimmed == null) {
            return LocalDateTime.now();
        }

        try {
            return LocalDateTime.parse(trimmed, SOURCE_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.now();
        }
    }

    private ApplyPeriod mapFixedPeriod(String value) {
        String periodText = YouthPolicyTextUtils.trimToNull(value);
        DateRange dateRange = parseDateRange(periodText);
        if (dateRange == null) {
            return new ApplyPeriod(
                    null,
                    null,
                    periodText,
                    RecruitmentType.FIXED_PERIOD,
                    RecruitmentStatus.UNKNOWN
            );
        }
        return createDatedPeriod(
                dateRange,
                periodText,
                RecruitmentType.FIXED_PERIOD,
                resolveDatedStatus(dateRange)
        );
    }

    private ApplyPeriod mapAlwaysOpenPeriod(
            String businessStartDate,
            String businessEndDate,
            String businessPeriodText,
            String registeredAt
    ) {
        String startText = YouthPolicyTextUtils.trimToNull(businessStartDate);
        String endText = YouthPolicyTextUtils.trimToNull(businessEndDate);
        String etcText = YouthPolicyTextUtils.trimToNull(businessPeriodText);

        ParsedPeriod parsedPeriod = findAlwaysOpenDatePeriod(
                startText,
                endText,
                etcText,
                registeredAt
        );

        //날짜 파싱이 안되는 경우
        if (parsedPeriod == null) {
            return new ApplyPeriod(
                    null,
                    null,
                    etcText,
                    RecruitmentType.ALWAYS,
                    RecruitmentStatus.OPEN
            );
        }

        return createDatedPeriod(
                parsedPeriod.dateRange(),
                parsedPeriod.periodText(),
                RecruitmentType.ALWAYS,
                resolveDatedStatus(parsedPeriod.dateRange())
        );
    }

    private ParsedPeriod findAlwaysOpenDatePeriod(
            String startText,
            String endText,
            String etcText,
            String registeredAt
    ) {
        DateRange separateDateRange = parseSeparateDates(startText, endText);

        //사업기간 날짜로 파싱 후 모집 날짜로 저장
        if (separateDateRange != null) {
            return new ParsedPeriod(separateDateRange, startText + " ~ " + endText);
        }

        //사업관련 기타 내용에 기간이 적혀있는 경우, 모집 날짜 파싱
        DateRange etcDateRange = parseDateRange(etcText);
        if (etcDateRange != null) {
            return new ParsedPeriod(etcDateRange, etcText);
        }

        //연중 모집 정책 모집날짜 파싱
        return createAnnualPeriod(etcText, startText, registeredAt);
    }

    private ParsedPeriod createAnnualPeriod(
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

        DateRange annualRange = new DateRange(
                LocalDate.of(year, 1, 1),
                LocalDate.of(year, 12, 31)
        );
        return new ParsedPeriod(annualRange, businessPeriodText);
    }

    private ApplyPeriod mapUnknownPeriod(String value) {
        String periodText = YouthPolicyTextUtils.trimToNull(value);
        DateRange dateRange = parseDateRange(periodText);
        if (dateRange == null) {
            return new ApplyPeriod(null, null, periodText, null, RecruitmentStatus.UNKNOWN);
        }
        return createDatedPeriod(dateRange, periodText, null, RecruitmentStatus.UNKNOWN);
    }

    private ApplyPeriod createDatedPeriod(
            DateRange dateRange,
            String periodText,
            RecruitmentType recruitmentType,
            RecruitmentStatus recruitmentStatus
    ) {
        return new ApplyPeriod(
                dateRange.startDate(),
                dateRange.endDate(),
                periodText,
                recruitmentType,
                recruitmentStatus
        );
    }

    private RecruitmentStatus resolveDatedStatus(DateRange dateRange) {
        LocalDate today = LocalDate.now();
        if (today.isBefore(dateRange.startDate())) {
            return RecruitmentStatus.UPCOMING;
        }
        if (today.isAfter(dateRange.endDate())) {
            return RecruitmentStatus.CLOSED;
        }
        return RecruitmentStatus.OPEN;
    }

    private DateRange parseSeparateDates(String startValue, String endValue) {
        LocalDate startDate = parseDate(startValue);
        LocalDate endDate = parseDate(endValue);
        return createDateRange(startDate, endDate);
    }

    private DateRange parseDateRange(String value) {
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

    private Integer findReferenceYear(String businessPeriodText, String businessStartDate, String registeredAt) {
        Integer textYear = extractYear(businessPeriodText);
        if (textYear != null) {
            return textYear;
        }

        LocalDate parsedBusinessStartDate = parseDate(businessStartDate);
        if (parsedBusinessStartDate != null) {
            return parsedBusinessStartDate.getYear();
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

    private record DateRange(LocalDate startDate, LocalDate endDate) {
    }

    private record ParsedPeriod(DateRange dateRange, String periodText) {
    }

    public record ApplyPeriod(
            LocalDate startDate,
            LocalDate endDate,
            String periodText,
            RecruitmentType recruitmentType,
            RecruitmentStatus recruitmentStatus
    ) {
    }
}
