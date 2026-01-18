package chungbazi.chungbazi_be.domain.policy.validator;

import chungbazi.chungbazi_be.domain.policy.dto.YouthPolicyResponse;
import chungbazi.chungbazi_be.global.utils.TimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicyValidator {

    public static final Set<String> VALID_KEYWORDS = Set.of(
            "계속", "상시", "매년", "연 2회", "별도 종료 시기 없음", "당해 연도", "상시 접속 가능"
    );

    public boolean isValid(YouthPolicyResponse response, LocalDate threshold){
        return response.getPlcyNo() != null && isDateValid(response, threshold);
    }

    public boolean isDateValid(YouthPolicyResponse response, LocalDate threshold) {
        LocalDate endDate = response.getEndDate();

        if (endDate != null){
            return endDate.isBefore(LocalDate.now(ZoneId.of("Asia/Seoul")).plusYears(1))
                    && endDate.isAfter(threshold);
        }

        //bizPrdEtcCn 파싱해서 endDate 업데이트
        String bizPrdEtcCn = response.getBizPrdEtcCn();

        if (bizPrdEtcCn == null || bizPrdEtcCn.isEmpty()){
            return false;
        }

        try {
            String normalized = bizPrdEtcCn.replace(" ", "");
            String[] split = normalized.split("~");

            if (split.length == 2){

                YearMonth endYearMonth = TimeFormatter.parseYearMonth(split[1]);

                if (endYearMonth != null){
                    LocalDate parsedEndDate = endYearMonth.atEndOfMonth();
                    response.updateEndDate(parsedEndDate);

                    return parsedEndDate.isBefore(LocalDate.now(ZoneId.of("Asia/Seoul")).plusYears(1))
                            && parsedEndDate.isAfter(threshold);
                }
            }
        } catch (Exception ignored){}

        return Optional.ofNullable(response.getBizPrdEtcCn())
                .map(s -> VALID_KEYWORDS.stream().anyMatch(s::contains))
                .orElse(false);
    }
}
