package com.chungbazi.server.domain.user.domain;

import com.chungbazi.server.domain.user.domain.type.*;
import com.chungbazi.server.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SocialType socialType;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "sido_code")
    private SidoCode sidoCode;

    @Column(name = "sido_name", length = 50)
    private String sidoName;

    @Column(name = "sigungu_code", length = 10)
    private String sigunguCode;

    @Column(name = "sigungu_name", length = 50)
    private String sigunguName;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_code")
    private EducationCode educationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_code")
    private JobCode jobCode;

    @Column(name = "income_level")
    private IncomeLevel incomeLevel;

    @Column(length = 512)
    private String fcmToken;

    @Column(nullable = false)
    private boolean onboardingCompleted;

    @Builder
    private User(
            String providerId,
            SocialType socialType,
            String email,
            String nickname,
            String fcmToken
    ) {
        this.providerId = providerId;
        this.socialType = socialType;
        this.email = email;
        this.nickname = nickname;
        this.fcmToken = fcmToken;
        this.onboardingCompleted = false;
    }

    public void completeOnboarding(
            SidoCode sidoCode,
            String sidoName,
            String sigunguCode,
            String sigunguName,
            EducationCode educationCode,
            JobCode jobCode,
            IncomeLevel incomeLevel
    ) {
        this.sidoCode = sidoCode;
        this.sidoName = sidoName;
        this.sigunguCode = sigunguCode;
        this.sigunguName = sigunguName;
        this.educationCode = educationCode;
        this.jobCode = jobCode;
        this.incomeLevel = incomeLevel;
        this.onboardingCompleted = true;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
