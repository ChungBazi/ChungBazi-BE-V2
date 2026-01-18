package chungbazi.chungbazi_be.domain.policy.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class PolicySearchResult {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer dDay;
    private String employment;
    private Integer priorityScore;  // 우선순위 점수 (내부 처리용)

    // 다음 커서 생성
    public String toCursor() {
        return priorityScore + "-" + id;
    }

    // 클라이언트 응답용 DTO로 변환 (우선순위 점수 제거)
    public PolicyListOneResponse toResponse() {
        return PolicyListOneResponse.builder()
                .policyId(id)
                .policyName(name)
                .startDate(startDate)
                .endDate(endDate)
                .dDay(dDay)
                .employment(employment)
                .build();
    }
}
