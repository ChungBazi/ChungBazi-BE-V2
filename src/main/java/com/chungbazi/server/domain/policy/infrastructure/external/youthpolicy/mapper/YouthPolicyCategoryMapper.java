package com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.mapper;

import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.dto.YouthPolicyItem;
import com.chungbazi.server.domain.policy.domain.vo.PolicySubCategoryType;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class YouthPolicyCategoryMapper {

    private static final Set<String> AMBIGUOUS_MIDDLE_CATEGORIES = Set.of(
            "예술인지원",
            "취약계층 및 금융지원",
            "정책인프라구축",
            "문화활동",
            "문화활동 및 생활지원",
            "미래역량강화"
    );

    private final YouthPolicyAmbiguousCategoryClassifier ambiguousCategoryClassifier;

    public PolicySubCategoryType toCategory(YouthPolicyItem item) {
        String middleCategory = YouthPolicyTextUtils.trimToNull(item.mclsfNm());
        if (AMBIGUOUS_MIDDLE_CATEGORIES.contains(middleCategory)) {
            return ambiguousCategoryClassifier.classify(item);
        }
        return mapFixedMiddleCategory(middleCategory);
    }

    private PolicySubCategoryType mapFixedMiddleCategory(String middleCategory) {
        return switch (middleCategory) {
            case "취업" -> PolicySubCategoryType.EMPLOYMENT_PREPARATION;
            case "재직자" -> PolicySubCategoryType.WORK_LIFE;
            case "창업" -> PolicySubCategoryType.STARTUP_BUSINESS;
            case "주택 및 거주지", "기숙사", "전월세 및 주거급여 지원" -> PolicySubCategoryType.HOUSING_COST_SPACE;
            case "교육비지원", "온라인교육", "온·오프라인교육" -> PolicySubCategoryType.EDUCATION_COMPETENCY;
            case "건강" -> PolicySubCategoryType.HEALTH_WELFARE;
            case "권익보호" -> PolicySubCategoryType.RIGHTS_PROTECTION;
            case "청년참여", "청년국제교류" -> PolicySubCategoryType.PARTICIPATION_EXCHANGE;
            case null, default -> null;
        };
    }
}
