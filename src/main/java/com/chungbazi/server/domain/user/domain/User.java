package com.chungbazi.server.domain.user.domain;

import com.chungbazi.server.domain.policy.domain.type.EducationCode;
import com.chungbazi.server.domain.policy.domain.type.EmploymentCode;
import com.chungbazi.server.domain.policy.domain.type.SidoCode;
import com.chungbazi.server.domain.user.domain.type.*;
import com.chungbazi.server.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "user",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_social_provider",
                columnNames = {"social_type", "provider_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type", nullable = false, length = 20)
    private SocialType socialType;

    @Column(name = "email", nullable = false, length = 254)
    private String email;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "sido_code")
    private SidoCode sidoCode;

    @Column(name = "sigungu_code", length = 10)
    private String sigunguCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_code")
    private EducationCode educationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_code")
    private EmploymentCode employmentCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "income_level", length = 20)
    private IncomeLevel incomeLevel;

    @Column(name = "fcm_token", length = 512)
    private String fcmToken;

    @Column(name = "onboarding_completed", nullable = false)
    private boolean onboardingCompleted;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static User create(
            String providerId,
            SocialType socialType,
            String email,
            String name,
            String fcmToken
    ) {
        User user = new User();
        user.providerId = providerId;
        user.socialType = socialType;
        user.email = email;
        user.name = name;
        user.fcmToken = fcmToken;
        user.onboardingCompleted = false;
        user.deleted = false;
        return user;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void updateName(String name) {
        this.name = name;
    }
}
