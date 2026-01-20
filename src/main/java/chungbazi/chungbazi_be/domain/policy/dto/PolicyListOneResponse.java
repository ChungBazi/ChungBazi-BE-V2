package chungbazi.chungbazi_be.domain.policy.dto;

import chungbazi.chungbazi_be.domain.policy.entity.Policy;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyListOneResponse {

    private Long policyId;
    private String policyName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer dDay;
    private String employment;

    public static PolicyListOneResponse from(Policy policy) {

        Integer dDay;

        if (policy.getEndDate() == null) {
            dDay = null;
        } else {
            dDay = (int) ChronoUnit.DAYS.between(policy.getEndDate(), LocalDate.now());
        }

        return PolicyListOneResponse.builder()
                .policyId(policy.getId())
                .policyName(policy.getName())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .dDay(dDay)
                .build();
    }

}