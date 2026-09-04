package com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.chungbazi.server.domain.policy.domain.type.internal.ApplyPeriod;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

class YouthPolicyDateMapperTest {

    private static final DateTimeFormatter SOURCE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final YouthPolicyDateMapper mapper = new YouthPolicyDateMapper(new YouthPolicyDateParser());

    @Test
    void mapsFixedPeriodFromApplicationDate() {
        ApplyPeriod result = map(
                YouthPolicyDateMapper.FIXED_PERIOD_CODE,
                "20990312 ~ 20990319",
                null,
                null,
                null
        );

        assertThat(result.startDate()).isEqualTo(LocalDate.of(2099, 3, 12));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2099, 3, 19));
        assertThat(result.recruitmentType()).isEqualTo(RecruitmentType.FIXED_PERIOD);
        assertThat(result.recruitmentStatus()).isEqualTo(RecruitmentStatus.UPCOMING);
    }

    @Test
    void mapsFixedPeriodToAlwaysOpenWhenPeriodIsAtLeastThousandDays() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = startDate.plusDays(1000);

        ApplyPeriod result = map(
                YouthPolicyDateMapper.FIXED_PERIOD_CODE,
                formatRange(startDate, endDate),
                null,
                null,
                null
        );

        assertThat(result.startDate()).isNull();
        assertThat(result.endDate()).isNull();
        assertThat(result.periodText()).isEqualTo("20260101 ~ 20280927");
        assertThat(result.recruitmentType()).isEqualTo(RecruitmentType.ALWAYS);
        assertThat(result.recruitmentStatus()).isEqualTo(RecruitmentStatus.OPEN);
    }

    @Test
    void keepsFixedPeriodWhenPeriodIsLessThanThousandDays() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = startDate.plusDays(999);

        ApplyPeriod result = map(
                YouthPolicyDateMapper.FIXED_PERIOD_CODE,
                formatRange(startDate, endDate),
                null,
                null,
                null
        );

        assertThat(result.startDate()).isEqualTo(startDate);
        assertThat(result.endDate()).isEqualTo(endDate);
        assertThat(result.recruitmentType()).isEqualTo(RecruitmentType.FIXED_PERIOD);
    }

    @Test
    void mapsAlwaysOpenCodeWithSeparateBusinessDatesAsFixedPeriod() {
        ApplyPeriod result = map(
                YouthPolicyDateMapper.ALWAYS_OPEN_CODE,
                null,
                "20260101",
                "20261231",
                null
        );

        assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(result.recruitmentType()).isEqualTo(RecruitmentType.FIXED_PERIOD);
    }

    @Test
    void mapsAlwaysOpenCodeWithLongBusinessDatesAsAlwaysOpen() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = startDate.plusDays(1000);

        ApplyPeriod result = map(
                YouthPolicyDateMapper.ALWAYS_OPEN_CODE,
                null,
                startDate.format(SOURCE_DATE_FORMATTER),
                endDate.format(SOURCE_DATE_FORMATTER),
                null
        );

        assertThat(result.startDate()).isNull();
        assertThat(result.endDate()).isNull();
        assertThat(result.recruitmentType()).isEqualTo(RecruitmentType.ALWAYS);
        assertThat(result.recruitmentStatus()).isEqualTo(RecruitmentStatus.OPEN);
    }

    @Test
    void mapsAlwaysOpenCodeWithRangeStoredInBusinessPeriodTextAsFixedPeriod() {
        ApplyPeriod result = map(
                YouthPolicyDateMapper.ALWAYS_OPEN_CODE,
                null,
                null,
                null,
                "2026-01-01~2026-12-31"
        );

        assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(result.recruitmentType()).isEqualTo(RecruitmentType.FIXED_PERIOD);
    }

    @Test
    void mapsAnnualAlwaysOpenPeriodUsingBusinessYearAsFixedPeriod() {
        ApplyPeriod result = map(
                YouthPolicyDateMapper.ALWAYS_OPEN_CODE,
                null,
                "20260401",
                null,
                "연중 운영"
        );

        assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(result.recruitmentType()).isEqualTo(RecruitmentType.FIXED_PERIOD);
    }

    @Test
    void mapsAlwaysOpenCodeWithYearMonthRangeAsFixedPeriod() {
        ApplyPeriod result = map(
                YouthPolicyDateMapper.ALWAYS_OPEN_CODE,
                null,
                null,
                null,
                "2026. 1. ~ 2026. 12."
        );

        assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(result.recruitmentType()).isEqualTo(RecruitmentType.FIXED_PERIOD);
    }

    @Test
    void mapsYearMonthRangeWhenEndYearIsOmitted() {
        ApplyPeriod result = map(
                YouthPolicyDateMapper.ALWAYS_OPEN_CODE,
                null,
                null,
                null,
                "2026. 3. ~ 11."
        );

        assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 11, 30));
        assertThat(result.recruitmentType()).isEqualTo(RecruitmentType.FIXED_PERIOD);
    }

    @Test
    void keepsAlwaysOpenStatusWhenDatesCannotBeDetermined() {
        ApplyPeriod result = map(
                YouthPolicyDateMapper.ALWAYS_OPEN_CODE,
                null,
                null,
                null,
                "상시 모집"
        );

        assertThat(result.startDate()).isNull();
        assertThat(result.endDate()).isNull();
        assertThat(result.recruitmentType()).isEqualTo(RecruitmentType.ALWAYS);
        assertThat(result.recruitmentStatus()).isEqualTo(RecruitmentStatus.OPEN);
    }

    private ApplyPeriod map(
            String code,
            String applyPeriod,
            String businessStartDate,
            String businessEndDate,
            String businessPeriod
    ) {
        return mapper.toApplyPeriod(
                code,
                applyPeriod,
                businessStartDate,
                businessEndDate,
                businessPeriod,
                "2025-12-01 10:00:00"
        );
    }

    private String formatRange(LocalDate startDate, LocalDate endDate) {
        return startDate.format(SOURCE_DATE_FORMATTER)
                + " ~ "
                + endDate.format(SOURCE_DATE_FORMATTER);
    }
}
