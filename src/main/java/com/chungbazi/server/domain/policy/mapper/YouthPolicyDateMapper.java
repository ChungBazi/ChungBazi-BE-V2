package com.chungbazi.server.domain.policy.mapper;

import com.chungbazi.server.domain.policy.client.dto.YouthPolicyItem;
import com.chungbazi.server.domain.policy.dto.internal.ApplyPeriod;
import com.chungbazi.server.domain.policy.dto.internal.DateRange;
import com.chungbazi.server.domain.policy.dto.internal.ParsedPeriod;
import com.chungbazi.server.domain.policy.enums.RecruitmentStatus;
import com.chungbazi.server.domain.policy.enums.RecruitmentType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class YouthPolicyDateMapper {

    static final String FIXED_PERIOD_CODE = "0057001";
    static final String ALWAYS_OPEN_CODE = "0057002";
    static final String CLOSED_CODE = "0057003";

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
                RecruitmentType.ALWAYS
        );
    }

    private ApplyPeriod mapUnknownPeriod(String value) {
        String periodText = YouthPolicyTextUtils.trimToNull(value);
        DateRange dateRange = dateParser.parseDateRange(periodText);

        if (dateRange == null) {
            return new ApplyPeriod(null, null, periodText, null, RecruitmentStatus.UNKNOWN);
        }
        return new ApplyPeriod(
                dateRange.startDate(),
                dateRange.endDate(),
                periodText,
                null,
                RecruitmentStatus.UNKNOWN
        );
    }

    private ApplyPeriod createDatedPeriod(
            DateRange dateRange,
            String periodText,
            RecruitmentType recruitmentType
    ) {
        return new ApplyPeriod(
                dateRange.startDate(),
                dateRange.endDate(),
                periodText,
                recruitmentType,
                resolveDatedStatus(dateRange)
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
}
