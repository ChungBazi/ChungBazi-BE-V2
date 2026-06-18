package com.chungbazi.server.domain.policy.entity;

import com.chungbazi.server.domain.policy.enums.EducationCode;
import com.chungbazi.server.domain.policy.enums.EmploymentCode;
import com.chungbazi.server.domain.policy.enums.RecruitmentStatus;
import com.chungbazi.server.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "policy")
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

    @Column(name = "apply_start_date")
    private LocalDate applyStartDate;

    @Column(name = "apply_end_date")
    private LocalDate applyEndDate;

    @Column(name = "apply_period_text", length = 100)
    private String applyPeriodText;

    @Enumerated(EnumType.STRING)
    @Column(name = "recruitment_status", nullable = false, length = 20)
    private RecruitmentStatus recruitmentStatus = RecruitmentStatus.UNKNOWN;

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

    public static Policy createPolicy(
            String plcyNo,
            String title,
            String summary,
            String supportContent,
            LocalDate applyStartDate,
            LocalDate applyEndDate,
            String applyPeriodText,
            RecruitmentStatus recruitmentStatus,
            Integer minAge,
            Integer maxAge,
            EducationCode educationCode,
            EmploymentCode jobCode,
            Integer minIncome,
            Integer maxIncome,
            String incomeDescription,
            String organizationName,
            int viewCount,
            int saveCount,
            LocalDateTime registeredAt
    ) {
        Policy policy = new Policy();
        policy.plcyNo = plcyNo;
        policy.title = title;
        policy.summary = summary;
        policy.supportContent = supportContent;
        policy.applyStartDate = applyStartDate;
        policy.applyEndDate = applyEndDate;
        policy.applyPeriodText = applyPeriodText;
        policy.recruitmentStatus = recruitmentStatus == null ? RecruitmentStatus.UNKNOWN : recruitmentStatus;
        policy.minAge = minAge;
        policy.maxAge = maxAge;
        policy.educationCode = educationCode;
        policy.employmentCode = jobCode;
        policy.minIncome = minIncome;
        policy.maxIncome = maxIncome;
        policy.incomeDescription = incomeDescription;
        policy.organizationName = organizationName;
        policy.viewCount = viewCount;
        policy.saveCount = saveCount;
        policy.registeredAt = registeredAt == null ? LocalDateTime.now() : registeredAt;
        return policy;
    }
}
