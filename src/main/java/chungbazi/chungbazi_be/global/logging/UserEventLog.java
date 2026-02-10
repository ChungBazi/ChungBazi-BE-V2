package chungbazi.chungbazi_be.global.logging;

import lombok.Builder;

import java.util.Map;

@Builder
public record UserEventLog(
        String eventName,
        String traceId,
        Long userId,
        String entryPoint,
        Map<String, Object> properties,
        String timeStamp
) {
}
