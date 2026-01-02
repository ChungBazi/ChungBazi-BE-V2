package chungbazi.chungbazi_be.domain.policy.entity;

import chungbazi.chungbazi_be.domain.policy.dto.YouthPolicyResponse;
import chungbazi.chungbazi_be.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Policy extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long id;

    //카테고리
    @Enumerated(EnumType.STRING)
    @NotNull
    private Category category;

    //정책명
    @NotNull
    private String name;

    //정책소개
    @Column(length = 1000)
    private String intro;

    //지원내용
    @Column(length = 1000)
    private String content;

    // 신청시작 날짜 (상시모집이면 null 값으로 저장)
    private LocalDate startDate;

    // 신청 끝나는 날짜 (상시모집이면 null 값으로 저장)
    private LocalDate endDate;

    // 기타 정책 기간
    @Column(length = 1000)
    private String bizPrdEtcCn;

    // 최소 연령
    @Column(length = 1000)
    private String minAge;

    // 최대 연령
    @Column(length = 1000)
    private String maxAge;

    /*
    // 전공 요건 내용
    @Column(length = 1000)
    private String major;
*/
    // 취업 상태 내용
    @Column(length = 1000)
    private String employment;

    // 소득 조건 코드
    //private String incomeCode;

    // 소득 최소 금액
    private String minIncome;

    // 소득 최대 금액
    private String maxIncome;

    // 기타내용 (incomeCode가 "0043003"이면 참고, "0043001"이면 비어있음)
    @Column(length = 1000)
    private String incomeEtc;

    // 추가신청자격
    @Column(columnDefinition = "TEXT")
    private String additionCondition;

    // 참여제한 대상
    @Column(columnDefinition = "TEXT")
    private String restrictedCondition;

    /*
    // 학력 요건 내용
    @Column(length = 1000)
    private String education;
    */

    // 신청 사이트 주소
    @Column(columnDefinition = "TEXT")
    private String registerUrl;

    // Open API 내 정책 ID
    @NotNull
    private String bizId;

    // 제출 서류 내용
    @Column(columnDefinition = "TEXT")
    private String document;

    // 심사 발표 내용
    @Column(length = 1000)
    private String result;

    // 정책 신청 방법 내용
    @Column(length = 1000)
    private String applyProcedure;

    // 참고 사이트 URL1
    @Column(columnDefinition = "TEXT")
    private String referenceUrl1;

    // 참고 사이트 URL2
    @Column(columnDefinition = "TEXT")
    private String referenceUrl2;

    // 포스터 사진 url
    private String posterUrl;

    public static Policy toEntity(YouthPolicyResponse dto) {

        String code = dto.getBscPlanPlcyWayNo();
        Category dtoCategory = Category.fromCode(code);

        return Policy.builder()
                .category(dtoCategory)
                .name(dto.getPlcyNm())
                .intro(dto.getPlcyExplnCn())
                .content(dto.getPlcySprtCn())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .bizPrdEtcCn(dto.getBizPrdEtcCn())
                .minAge(dto.getSprtTrgtMinAge())
                .maxAge(dto.getSprtTrgtMaxAge())
                .minIncome(dto.getEarnMinAmt())
                .maxIncome(dto.getEarnMaxAmt())
                .incomeEtc(dto.getEarnEtcCn())
                .additionCondition(dto.getAddAplyQlfcCndCn())
                .registerUrl(dto.getAplyUrlAddr())
                .bizId(dto.getPlcyNo())
                .document(dto.getSbmsnDcmntCn())
                .result(dto.getSrngMthdCn())
                .applyProcedure(dto.getPlcyAplyMthdCn())
                .referenceUrl1(dto.getRefUrlAddr1())
                .referenceUrl2(dto.getRefUrlAddr2())
                .build();
    }

    public String getStatus(LocalDate today, Set<String> validKeywords) {
        if (bizPrdEtcCn != null) {
            for (String keyword : validKeywords) {
                if (bizPrdEtcCn.contains(keyword)) {
                    return "상시";
                }
            }
        }

        if (startDate != null && startDate.isAfter(today)) {
            return "예정";
        }

        if (startDate != null && endDate != null) {
            if ((startDate.isEqual(today) || startDate.isBefore(today)) &&
                    (endDate.isEqual(today) || endDate.isAfter(today))) {
                return "진행중";
            }
        }

        if (endDate != null && endDate.isBefore(today)) {
            return "마감";
        }

        return "상시";

    }
}