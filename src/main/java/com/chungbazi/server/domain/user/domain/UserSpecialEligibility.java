package com.chungbazi.server.domain.user.domain;

import com.chungbazi.server.domain.user.domain.type.SpecialEligibilityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "user_special_eligibility",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_special_eligibility_user_type",
                columnNames = {"user_id", "eligibility_type"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSpecialEligibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_special_eligibility_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "eligibility_type", nullable = false, length = 40)
    private SpecialEligibilityType eligibilityType;

    public static UserSpecialEligibility create(
            User user,
            SpecialEligibilityType eligibilityType
    ) {
        if (eligibilityType == null) {
            throw new IllegalArgumentException("특별 지원 자격 선택은 필수입니다.");
        }
        UserSpecialEligibility eligibility = new UserSpecialEligibility();
        eligibility.user = user;
        eligibility.eligibilityType = eligibilityType;
        return eligibility;
    }
}
