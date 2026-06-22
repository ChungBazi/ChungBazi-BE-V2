package com.chungbazi.server.domain.policy.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PolicySubCategoryType {
    EMPLOYMENT_PREPARATION("취업준비", PolicyCategoryType.JOB_STARTUP),
    WORK_LIFE("재직/직장생활", PolicyCategoryType.JOB_STARTUP),
    STARTUP_BUSINESS("창업/사업화", PolicyCategoryType.JOB_STARTUP),
    HOUSING_COST_SPACE("주거비/주거공간", PolicyCategoryType.HOUSING),
    EDUCATION_COMPETENCY("교육/역량강화", PolicyCategoryType.GROWTH),
    FINANCE_LIVING("금융/생활비", PolicyCategoryType.LIFE_SUPPORT),
    HEALTH_WELFARE("건강/복지", PolicyCategoryType.LIFE_SUPPORT),
    RIGHTS_PROTECTION("권익보호", PolicyCategoryType.LIFE_SUPPORT),
    CULTURE_ART("문화/예술", PolicyCategoryType.ACTIVITY),
    PARTICIPATION_EXCHANGE("참여/교류", PolicyCategoryType.ACTIVITY);

    private final String description;
    private final PolicyCategoryType category;
}
