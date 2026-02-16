package chungbazi.chungbazi_be.domain.notification.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FcmTokenRequestDTO(
        @NotBlank
        String fcmToken
) {
}
