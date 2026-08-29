package com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.dto.YouthPolicyItem;
import org.junit.jupiter.api.Test;

class YouthPolicyCategoryMapperTest {

    private final YouthPolicyCategoryMapper mapper = new YouthPolicyCategoryMapper(
            new YouthPolicyAmbiguousCategoryClassifier()
    );

    @Test
    void returnsNullWhenMiddleCategoryAndTextSignalsAreMissing() {
        PolicySubCategoryType result = mapper.toCategory(item(null));

        assertThat(result).isNull();
    }

    @Test
    void classifiesByTextSignalWhenMiddleCategoryIsNull() {
        PolicySubCategoryType result = mapper.toCategory(item(null, "청년 월세 지원"));

        assertThat(result).isEqualTo(PolicySubCategoryType.HOUSING_COST_SPACE);
    }

    @Test
    void mapsFirstMiddleCategoryWhenMultipleCategoriesAreProvided() {
        PolicySubCategoryType result = mapper.toCategory(item("창업,취업"));

        assertThat(result).isEqualTo(PolicySubCategoryType.STARTUP_BUSINESS);
    }

    private static YouthPolicyItem item(String middleCategory) {
        return item(middleCategory, null);
    }

    private static YouthPolicyItem item(String middleCategory, String title) {
        return new YouthPolicyItem(
                null,
                title,
                null,
                null,
                null,
                middleCategory,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
