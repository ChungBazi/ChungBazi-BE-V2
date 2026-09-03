package com.chungbazi.server.fixture;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.type.IncomeConditionType;
import com.chungbazi.server.domain.policy.domain.type.PolicyDisplayStatus;
import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentType;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class PolicyFixture {

    private PolicyFixture() {
    }

    public static PolicyBuilder policy() {
        return new PolicyBuilder();
    }

    public static final class PolicyBuilder {

        private Long id = 1L;
        private String policyNumber;
        private String title = "테스트 정책";
        private PolicySubCategoryType subCategory = PolicySubCategoryType.EMPLOYMENT_PREPARATION;
        private PolicyDisplayStatus displayStatus = PolicyDisplayStatus.VISIBLE;
        private Integer minAge;
        private Integer maxAge;
        private IncomeConditionType incomeConditionType = IncomeConditionType.NO_LIMIT;
        private String incomeDescription;
        private int viewCount;
        private int saveCount;
        private LocalDate applyEndDate;
        private RecruitmentType recruitmentType = RecruitmentType.ALWAYS;
        private RecruitmentStatus recruitmentStatus = RecruitmentStatus.OPEN;
        private LocalDateTime registeredAt = LocalDateTime.of(2026, 1, 1, 0, 0);

        public PolicyBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public PolicyBuilder policyNumber(String policyNumber) {
            this.policyNumber = policyNumber;
            return this;
        }

        public PolicyBuilder title(String title) {
            this.title = title;
            return this;
        }

        public PolicyBuilder subCategory(PolicySubCategoryType subCategory) {
            this.subCategory = subCategory;
            return this;
        }

        public PolicyBuilder displayStatus(PolicyDisplayStatus displayStatus) {
            this.displayStatus = displayStatus;
            return this;
        }

        public PolicyBuilder age(Integer minAge, Integer maxAge) {
            this.minAge = minAge;
            this.maxAge = maxAge;
            return this;
        }

        public PolicyBuilder income(IncomeConditionType type, String description) {
            this.incomeConditionType = type;
            this.incomeDescription = description;
            return this;
        }

        public PolicyBuilder counts(int viewCount, int saveCount) {
            this.viewCount = viewCount;
            this.saveCount = saveCount;
            return this;
        }

        public PolicyBuilder applyEndDate(LocalDate applyEndDate) {
            this.applyEndDate = applyEndDate;
            return this;
        }

        public PolicyBuilder recruitmentType(RecruitmentType recruitmentType) {
            this.recruitmentType = recruitmentType;
            return this;
        }

        public PolicyBuilder recruitmentStatus(RecruitmentStatus recruitmentStatus) {
            this.recruitmentStatus = recruitmentStatus;
            return this;
        }

        public PolicyBuilder registeredAt(LocalDateTime registeredAt) {
            this.registeredAt = registeredAt;
            return this;
        }

        public Policy build() {
            Policy policy = Policy.createPolicy(
                    policyNumber == null ? "TEST-" + id : policyNumber,
                    title,
                    null,
                    null,
                    null,
                    subCategory,
                    true,
                    null,
                    applyEndDate,
                    null,
                    recruitmentType,
                    recruitmentStatus,
                    minAge,
                    maxAge,
                    null,
                    null,
                    incomeConditionType,
                    null,
                    null,
                    incomeDescription,
                    null,
                    registeredAt,
                    registeredAt
            );

            ReflectionTestUtils.setField(policy, "id", id);
            ReflectionTestUtils.setField(policy, "displayStatus", displayStatus);
            ReflectionTestUtils.setField(policy, "viewCount", viewCount);
            ReflectionTestUtils.setField(policy, "saveCount", saveCount);
            return policy;
        }
    }
}
