package com.chungbazi.server.domain.policy.api.dto.response;

import com.chungbazi.server.domain.policy.domain.type.SidoCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "시도 코드 조회 API")
public record SidoResponse(
        @Schema(description = "시도 코드", example = "SEOUL")
        String sidoCode,

        @Schema(description = "시도명", example = "서울특별시")
        String sidoName
) {
    public static SidoResponse from(SidoCode sidoCode) {
        return SidoResponse.builder()
                .sidoCode(sidoCode.name())
                .sidoName(sidoCode.getName())
                .build();
    }
}
