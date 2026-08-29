package com.chungbazi.server.domain.policy.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Schema(description = "찜한 정책 마감일 캘린더 응답")
public record CalendarResponse(
        @Schema(description = "조회 대상 연월", example = "2026-08")
        YearMonth targetMonth,

        @Schema(description = "마감일이 존재하는 날짜 목록")
        List<LocalDate> deadlineDates
) {
}
