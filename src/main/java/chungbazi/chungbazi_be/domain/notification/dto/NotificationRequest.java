package chungbazi.chungbazi_be.domain.notification.dto;

import chungbazi.chungbazi_be.domain.notification.entity.enums.NotificationType;
import chungbazi.chungbazi_be.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    @NotNull
    private User user;

    @NotNull
    private NotificationType type;

    @NotBlank
    private String message;

    private Long targetId;
}
