package chungbazi.chungbazi_be.domain.notification.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingRequestDTO {

    private boolean policyAlarm;
    private boolean communityAlarm;
    private boolean rewardAlarm;
    private boolean noticeAlarm;

}
