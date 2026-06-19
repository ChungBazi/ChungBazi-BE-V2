package com.chungbazi.server.domain.policy.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.chungbazi.server.domain.policy.dto.internal.ApplyPeriod;
import com.chungbazi.server.domain.policy.enums.RecruitmentStatus;
import com.chungbazi.server.domain.policy.enums.RecruitmentType;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class YouthPolicyDateMapperTest {

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
    void mapsAlwaysOpenPeriodFromSeparateBusinessDates() {
        ApplyPeriod result = map(
                YouthPolicyDateMapper.ALWAYS_OPEN_CODE,
                null,
                "20260101",
                "20261231",
                null
        );

        assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(result.recruitmentType()).isEqualTo(RecruitmentType.ALWAYS);
    }

    @Test
    void mapsAlwaysOpenRangeStoredInBusinessPeriodText() {
        ApplyPeriod result = map(
                YouthPolicyDateMapper.ALWAYS_OPEN_CODE,
                null,
                null,
                null,
                "2026-01-01~2026-12-31"
        );

        assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(result.recruitmentType()).isEqualTo(RecruitmentType.ALWAYS);
    }

    @Test
    void mapsAnnualAlwaysOpenPeriodUsingBusinessYear() {
        ApplyPeriod result = map(
                YouthPolicyDateMapper.ALWAYS_OPEN_CODE,
                null,
                "20260401",
                null,
                "연중 운영"
        );

        assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(result.recruitmentType()).isEqualTo(RecruitmentType.ALWAYS);
    }

    @Test
    void mapsAlwaysOpenYearMonthRange() {
        ApplyPeriod result = map(
                YouthPolicyDateMapper.ALWAYS_OPEN_CODE,
                null,
                null,
                null,
                "2026. 1. ~ 2026. 12."
        );

        assertThat(result.startDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(result.endDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(result.recruitmentType()).isEqualTo(RecruitmentType.ALWAYS);
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
}
