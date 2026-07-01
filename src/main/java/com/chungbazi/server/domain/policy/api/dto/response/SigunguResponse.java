package com.chungbazi.server.domain.policy.api.dto.response;

import com.chungbazi.server.domain.policy.domain.entity.RegionCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "시군구 코드 조회 API")
public record SigunguResponse(
        @Schema(description = "시군구 코드", example = "11110")
        String sigunguCode,

        @Schema(description = "시군구명", example = "종로구")
        String sigunguName
) {
    public static SigunguResponse from(RegionCode regionCode) {
        return SigunguResponse.builder()
                .sigunguCode(regionCode.getSigunguCode())
                .sigunguName(regionCode.getSigunguName())
                .build();
    }
}
