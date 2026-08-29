package com.chungbazi.server.domain.user.api.dto.request;

import com.chungbazi.server.domain.user.domain.type.WithdrawalReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.Set;

@Builder
@Schema(description = "사용자 탈퇴 API")
public record UserWithdrawalRequest(
        @NotEmpty(message = "탈퇴 사유를 한 개 이상 선택해야 합니다.")
        @Schema(
                description = "탈퇴 사유",
                example = "[\"POLICY_DISCOVERY_DIFFICULT\", \"NO_LONGER_NEEDED\"]"
        )
        Set<WithdrawalReason> reasons,

        @Schema(
                description = "탈퇴 상세 의견",
                example = "원하는 정책 정보를 찾기 어려웠어요."
        )
        String detail
) {
}
