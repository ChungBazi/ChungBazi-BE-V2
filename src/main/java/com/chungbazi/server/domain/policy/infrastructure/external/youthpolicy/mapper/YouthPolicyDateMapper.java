package com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.mapper;

import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.dto.YouthPolicyItem;
import com.chungbazi.server.domain.policy.domain.type.internal.ApplyPeriod;
import com.chungbazi.server.domain.policy.domain.type.internal.DateRange;
import com.chungbazi.server.domain.policy.domain.type.internal.ParsedPeriod;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class YouthPolicyDateMapper {

    static final String FIXED_PERIOD_CODE = "0057001";
    static final String ALWAYS_OPEN_CODE = "0057002";
    static final String CLOSED_CODE = "0057003";
    private static final int ALWAYS_OPEN_THRESHOLD_DAYS = 1000;

    private final YouthPolicyDateParser dateParser;

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

    public LocalDateTime toRegisteredAt(String value) {
        return dateParser.parseRegisteredAtOrNow(value);
    }

    public LocalDateTime toSourceModifiedAt(YouthPolicyItem item) {
        return dateParser.parseDateTimeOrNull(item.lastMdfcnDt(), item.frstRegDt());
    }

    private ApplyPeriod mapFixedPeriod(String value) {
        String periodText = YouthPolicyTextUtils.trimToNull(value);
        DateRange dateRange = dateParser.parseDateRange(periodText);
        if (dateRange == null) {
            return new ApplyPeriod(
                    null,
                    null,
                    periodText,
                    RecruitmentType.FIXED_PERIOD,
                    RecruitmentStatus.UNKNOWN
            );
        }
        return createDatedPeriod(dateRange, periodText, RecruitmentType.FIXED_PERIOD);
    }

    private ApplyPeriod mapAlwaysOpenPeriod(
            String businessStartDate,
            String businessEndDate,
            String businessPeriodText,
            String registeredAt
    ) {
        ParsedPeriod parsedPeriod = dateParser.findAlwaysOpenPeriod(
                businessStartDate,
                businessEndDate,
                businessPeriodText,
                registeredAt
        );

        //날짜 파싱이 안되는 경우
        if (parsedPeriod == null) {
            return new ApplyPeriod(
                    null,
                    null,
                    YouthPolicyTextUtils.trimToNull(businessPeriodText),
                    RecruitmentType.ALWAYS,
                    RecruitmentStatus.OPEN
            );
        }
        return createDatedPeriod(
                parsedPeriod.dateRange(),
                parsedPeriod.periodText(),
                RecruitmentType.FIXED_PERIOD
        );
    }

    private ApplyPeriod mapUnknownPeriod(String value) {
        String periodText = YouthPolicyTextUtils.trimToNull(value);
        DateRange dateRange = dateParser.parseDateRange(periodText);

        if (dateRange == null) {
            return new ApplyPeriod(null, null, periodText, null, RecruitmentStatus.UNKNOWN);
        }
        return createDatedPeriod(dateRange, periodText, null);
    }

    private ApplyPeriod createDatedPeriod(
            DateRange dateRange,
            String periodText,
            RecruitmentType recruitmentType
    ) {
        if (isAlwaysOpenPeriod(dateRange)) {
            return new ApplyPeriod(
                    null,
                    null,
                    periodText,
                    RecruitmentType.ALWAYS,
                    RecruitmentStatus.OPEN
            );
        }

        return new ApplyPeriod(
                dateRange.startDate(),
                dateRange.endDate(),
                periodText,
                recruitmentType,
                resolveDatedStatus(dateRange)
        );
    }

    private boolean isAlwaysOpenPeriod(DateRange dateRange) {
        return ChronoUnit.DAYS.between(dateRange.startDate(), dateRange.endDate()) >= ALWAYS_OPEN_THRESHOLD_DAYS;
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
}
