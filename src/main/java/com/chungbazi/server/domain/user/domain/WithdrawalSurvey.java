package com.chungbazi.server.domain.user.domain;

import com.chungbazi.server.domain.user.domain.type.WithdrawalReason;
import com.chungbazi.server.domain.user.exception.UserException;
import com.chungbazi.server.domain.user.exception.code.UserErrorCode;
import com.chungbazi.server.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = "withdrawal_survey")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawalSurvey extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "withdrawal_survey_id")
    private Long id;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "withdrawal_survey_reason",
            joinColumns = @JoinColumn(
                    name = "withdrawal_survey_id",
                    foreignKey = @ForeignKey(
                            name = "fk_withdrawal_survey_reason"
                    )
            ),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_withdrawal_survey_reason",
                    columnNames = {
                            "withdrawal_survey_id",
                            "reason"
                    }
            )
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 50)
    private Set<WithdrawalReason> reasons = new HashSet<>();

    @Column(name = "detail", length = 500)
    private String detail;

    public static WithdrawalSurvey create(
            Set<WithdrawalReason> reasons,
            String detail
    ) {
        validateReasons(reasons);
        WithdrawalSurvey survey = new WithdrawalSurvey();
        survey.reasons.addAll(reasons);
        survey.detail = normalizeDetail(detail);
        return survey;
    }

    private static void validateReasons(Set<WithdrawalReason> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            throw new UserException(UserErrorCode.INVALID_WITHDRAWAL_REASON);
        }
    }

    private static String normalizeDetail(String detail) {
        if (detail == null || detail.isBlank()) {
            return null;
        }
        return detail.trim();
    }
}
