package com.chungbazi.server.domain.user.api.dto.request;

import com.chungbazi.server.domain.user.domain.type.WithdrawalReason;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Set;

@Builder
@Schema(description = "사용자 탈퇴 API")
public record UserWithdrawalRequest(
        @NotEmpty(message = "탈퇴 사유를 한 개 이상 선택해야 합니다.")
        Set<WithdrawalReason> reasons,

        @Size(max = 500, message = "상세 의견은 500자 이하여야 합니다.")
        String detail
) {
}
