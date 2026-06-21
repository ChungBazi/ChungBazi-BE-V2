package com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.mapper;

import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.dto.YouthPolicyItem;
import com.chungbazi.server.domain.policy.domain.vo.PolicySubCategoryType;
import org.springframework.stereotype.Component;

/*
정책 텍스트 조합
분야별 키워드 판정
판정 우선순위와 fallback
 */
@Component
public class YouthPolicyAmbiguousCategoryClassifier {

    public PolicySubCategoryType classify(YouthPolicyItem item) {
        String text = YouthPolicyTextUtils.joinNonBlank(
                " ",
                item.plcyNm(),
                item.plcyKywdNm(),
                item.plcyExplnCn(),
                item.plcySprtCn(),
                item.plcyPvsnMthdCd()
        );

        if (isJobStartupSignal(text, item.plcyPvsnMthdCd())) {
            return toJobStartupSubCategory(text);
        }
        if (isHousingSignal(text)) {
            return PolicySubCategoryType.HOUSING_COST_SPACE;
        }
        if (isGrowthSignal(text)) {
            return PolicySubCategoryType.EDUCATION_COMPETENCY;
        }
        if (isRightsSignal(text)) {
            return PolicySubCategoryType.RIGHTS_PROTECTION;
        }
        if (isHealthSignal(text)) {
            return PolicySubCategoryType.HEALTH_WELFARE;
        }
        if (isFinanceLivingSignal(text)) {
            return PolicySubCategoryType.FINANCE_LIVING;
        }
        if (isActivitySignal(text)) {
            return toActivitySubCategory(text);
        }
        return fallbackByMiddleCategory(item.mclsfNm());
    }

    private PolicySubCategoryType toJobStartupSubCategory(String text) {
        if (containsAny(text, "재직", "직장", "근로자")) {
            return PolicySubCategoryType.WORK_LIFE;
        }
        if (containsAny(text, "창업", "사업화")) {
            return PolicySubCategoryType.STARTUP_BUSINESS;
        }
        return PolicySubCategoryType.EMPLOYMENT_PREPARATION;
    }

    private PolicySubCategoryType toActivitySubCategory(String text) {
        if (containsAny(text, "청년참여", "참여", "교류", "국제교류", "서포터즈", "봉사")) {
            return PolicySubCategoryType.PARTICIPATION_EXCHANGE;
        }
        return PolicySubCategoryType.CULTURE_ART;
    }

    private PolicySubCategoryType fallbackByMiddleCategory(String middleCategory) {
        return switch (YouthPolicyTextUtils.trimToNull(middleCategory)) {
            case "미래역량강화" -> PolicySubCategoryType.EDUCATION_COMPETENCY;
            case "정책인프라구축" -> PolicySubCategoryType.PARTICIPATION_EXCHANGE;
            case "문화활동", "문화활동 및 생활지원", "예술인지원" -> PolicySubCategoryType.CULTURE_ART;
            case "취약계층 및 금융지원" -> PolicySubCategoryType.FINANCE_LIVING;
            case null, default -> PolicySubCategoryType.FINANCE_LIVING;
        };
    }

    private boolean isJobStartupSignal(String text, String policyProvidingMethodCode) {
        return containsAny(text, "취업", "채용", "일자리", "인턴", "재직", "창업", "사업화", "공모", "제작비")
                || "0042002".equals(YouthPolicyTextUtils.trimToNull(policyProvidingMethodCode));
    }

    private boolean isHousingSignal(String text) {
        return containsAny(text, "주거", "주택", "기숙사", "전월세", "월세", "임대", "거주", "이사");
    }

    private boolean isGrowthSignal(String text) {
        return containsAny(text, "교육", "강의", "수강", "멘토링", "역량", "자격증", "훈련", "학습");
    }

    private boolean isActivitySignal(String text) {
        return containsAny(text, "공연", "전시", "교류", "문화활동", "참여", "서포터즈", "봉사", "국제교류");
    }

    private boolean isRightsSignal(String text) {
        return containsAny(text, "권익", "권리", "법률", "노무", "보호", "피해", "안전");
    }

    private boolean isHealthSignal(String text) {
        return containsAny(text, "건강", "의료", "심리", "마음", "치료", "검진", "운동");
    }

    private boolean isFinanceLivingSignal(String text) {
        return containsAny(
                text, "금융", "대출", "저축", "자산", "생활비", "생계", "소득", "수당", "지원금", "장학금", "교통비", "식비");
    }

    private boolean containsAny(String text, String... keywords) {
        if (text == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
