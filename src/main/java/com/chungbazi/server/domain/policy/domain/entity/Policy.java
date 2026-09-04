package com.chungbazi.server.domain.policy.domain.entity;

import com.chungbazi.server.domain.policy.domain.type.*;
import com.chungbazi.server.domain.policy.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.exception.PolicyException;
import com.chungbazi.server.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "policy",
        indexes = {
                @Index(name = "idx_policy_category", columnList = "category"),
                @Index(name = "idx_policy_sub_category", columnList = "sub_category"),
                @Index(
                        name = "idx_policy_category_status_registered",
                        columnList = "category,recruitment_status,registered_at,policy_id"
                ),
                @Index(
                        name = "idx_policy_status_registered",
                        columnList = "recruitment_status,registered_at,policy_id"
                ),
                @Index(
                        name = "idx_policy_category_status_deadline",
                        columnList = "category,recruitment_status,apply_end_date,policy_id"
                ),
                @Index(
                        name = "idx_policy_status_deadline",
                        columnList = "recruitment_status,apply_end_date,policy_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Policy extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long id;

    @Column(name = "plcy_no", nullable = false, unique = true, length = 30)
    private String plcyNo;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "summary", columnDefinition = "text")
    private String summary;

    @Column(name = "support_content", columnDefinition = "text")
    private String supportContent;

    @Column(name = "apply_url", columnDefinition = "text")
    private String applyUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private PolicyCategoryType category;

    @Enumerated(EnumType.STRING)
    @Column(name = "sub_category", nullable = false, length = 40)
    private PolicySubCategoryType subCategory;

    @Column(name = "is_national", nullable = false)
    private boolean national;

    @Column(name = "apply_start_date")
    private LocalDate applyStartDate;

    @Column(name = "apply_end_date")
    private LocalDate applyEndDate;

    @Column(name = "apply_period_text", columnDefinition = "text")
    private String applyPeriodText;

    @Enumerated(EnumType.STRING)
    @Column(name = "recruitment_type", nullable = false, length = 20)
    private RecruitmentType recruitmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "recruitment_status", nullable = false, length = 20)
    private RecruitmentStatus recruitmentStatus = RecruitmentStatus.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(name = "display_status", nullable = false, length = 20)
    private PolicyDisplayStatus displayStatus = PolicyDisplayStatus.VISIBLE;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_code", length = 30)
    private EducationCode educationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_code", length = 30)
    private EmploymentCode employmentCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "income_condition_type", nullable = false, length = 20)
    private IncomeConditionType incomeConditionType;

    @Column(name = "min_income")
    private Integer minIncome;

    @Column(name = "max_income")
    private Integer maxIncome;

    @Column(name = "income_description", columnDefinition = "text")
    private String incomeDescription;

    @Column(name = "organization_name", length = 100)
    private String organizationName;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "save_count", nullable = false)
    private int saveCount;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "source_modified_at")
    private LocalDateTime sourceModifiedAt;

    public static Policy createPolicy(
            String plcyNo,
            String title,
            String summary,
            String supportContent,
            String applyUrl,
            PolicySubCategoryType subCategory,
            boolean national,
            LocalDate applyStartDate,
            LocalDate applyEndDate,
            String applyPeriodText,
            RecruitmentType recruitmentType,
            RecruitmentStatus recruitmentStatus,
            Integer minAge,
            Integer maxAge,
            EducationCode educationCode,
            EmploymentCode jobCode,
            IncomeConditionType incomeConditionType,
            Integer minIncome,
            Integer maxIncome,
            String incomeDescription,
            String organizationName,
            LocalDateTime registeredAt,
            LocalDateTime sourceModifiedAt
    ) {
        if (subCategory == null) {
            throw new PolicyException(PolicyErrorCode.INVALID_POLICY_CATEGORY);
        }

        Policy policy = new Policy();
        policy.plcyNo = plcyNo;
        policy.title = title;
        policy.summary = summary;
        policy.supportContent = supportContent;
        policy.applyUrl = applyUrl;
        policy.category = subCategory.getCategory();
        policy.subCategory = subCategory;
        policy.national = national;
        policy.applyStartDate = applyStartDate;
        policy.applyEndDate = applyEndDate;
        policy.applyPeriodText = applyPeriodText;
        policy.recruitmentType = recruitmentType;
        policy.recruitmentStatus = recruitmentStatus == null ? RecruitmentStatus.UNKNOWN : recruitmentStatus;
        policy.minAge = minAge;
        policy.maxAge = maxAge;
        policy.educationCode = educationCode;
        policy.employmentCode = jobCode;
        policy.incomeConditionType = incomeConditionType;
        policy.minIncome = minIncome;
        policy.maxIncome = maxIncome;
        policy.incomeDescription = incomeDescription;
        policy.organizationName = organizationName;
        policy.viewCount = 0;
        policy.saveCount = 0;
        policy.registeredAt = registeredAt == null ? LocalDateTime.now() : registeredAt;
        policy.sourceModifiedAt = sourceModifiedAt;
        return policy;
    }

    public void updatePolicy(
            String title,
            String summary,
            String supportContent,
            String applyUrl,
            PolicySubCategoryType subCategory,
            boolean national,
            LocalDate applyStartDate,
            LocalDate applyEndDate,
            String applyPeriodText,
            RecruitmentType recruitmentType,
            RecruitmentStatus recruitmentStatus,
            Integer minAge,
            Integer maxAge,
            EducationCode educationCode,
            EmploymentCode jobCode,
            IncomeConditionType incomeConditionType,
            Integer minIncome,
            Integer maxIncome,
            String incomeDescription,
            String organizationName,
            LocalDateTime sourceModifiedAt
    ) {
        if (subCategory == null) {
            throw new PolicyException(PolicyErrorCode.INVALID_POLICY_CATEGORY);
        }

        this.title = title;
        this.summary = summary;
        this.supportContent = supportContent;
        this.applyUrl = applyUrl;
        this.category = subCategory.getCategory();
        this.subCategory = subCategory;
        this.national = national;
        this.applyStartDate = applyStartDate;
        this.applyEndDate = applyEndDate;
        this.applyPeriodText = applyPeriodText;
        this.recruitmentType = recruitmentType;
        RecruitmentStatus resolvedRecruitmentStatus = recruitmentStatus == null
                ? RecruitmentStatus.UNKNOWN
                : recruitmentStatus;
        this.recruitmentStatus = resolvedRecruitmentStatus;
        if (this.displayStatus == PolicyDisplayStatus.HIDDEN_EXPIRED
                && resolvedRecruitmentStatus != RecruitmentStatus.CLOSED) {
            this.displayStatus = PolicyDisplayStatus.VISIBLE;
        }
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.educationCode = educationCode;
        this.employmentCode = jobCode;
        this.incomeConditionType = incomeConditionType;
        this.minIncome = minIncome;
        this.maxIncome = maxIncome;
        this.incomeDescription = incomeDescription;
        this.organizationName = organizationName;
        this.sourceModifiedAt = sourceModifiedAt;
    }

    public void updateRecruitmentStatus(RecruitmentStatus recruitmentStatus, LocalDateTime sourceModifiedAt) {
        this.recruitmentStatus = recruitmentStatus == null ? RecruitmentStatus.UNKNOWN : recruitmentStatus;
        this.sourceModifiedAt = sourceModifiedAt;
    }
}
