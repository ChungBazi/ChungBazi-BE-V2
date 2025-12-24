package chungbazi.chungbazi_be.domain.user.entity;

import chungbazi.chungbazi_be.domain.character.entity.Character;
import chungbazi.chungbazi_be.domain.chat.entity.Message;
import chungbazi.chungbazi_be.domain.community.entity.Comment;
import chungbazi.chungbazi_be.domain.community.entity.Post;
import chungbazi.chungbazi_be.domain.chat.entity.ChatRoomSetting;
import chungbazi.chungbazi_be.domain.notification.entity.Notification;
import chungbazi.chungbazi_be.domain.notification.entity.NotificationSetting;
import chungbazi.chungbazi_be.domain.user.entity.enums.*;
import chungbazi.chungbazi_be.domain.user.entity.mapping.UserAddition;
import chungbazi.chungbazi_be.domain.user.entity.mapping.UserInterest;
import chungbazi.chungbazi_be.global.entity.Uuid;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@DynamicUpdate
@DynamicInsert
@Builder
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private OAuthProvider oAuthProvider;

    @Enumerated(EnumType.STRING)
    private Education education;

    @Enumerated(EnumType.STRING)
    private Employment employment;

    @Enumerated(EnumType.STRING)
    private Income income;

    @Enumerated(EnumType.STRING)
    private Region region;

    @Column(nullable = false)
    private boolean isDeleted;

    @ColumnDefault("false")
    private boolean surveyStatus;

    // 캐릭터 관련
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RewardLevel characterImg = RewardLevel.LEVEL_1;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.ALL})
    private List<Character> characters;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RewardLevel reward = RewardLevel.LEVEL_1;

    // 삭제 예정
    @OneToOne
    @JoinColumn(name = "uuid_id")
    private Uuid uuid;

    // 커뮤니티 관련
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // 유저 정보 관련
    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = {CascadeType.ALL})
    private List<UserAddition> userAdditionList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = {CascadeType.ALL})
    private List<UserInterest> userInterestList = new ArrayList<>();

    // 알람 관련
    @OneToMany(mappedBy = "user",cascade = {CascadeType.ALL})
    private List<Notification> notificationList = new ArrayList<>();

    @OneToOne(mappedBy = "user",cascade = {CascadeType.ALL})
    private NotificationSetting notificationSetting;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.ALL})
    @Builder.Default
    private List<ChatRoomSetting> chatRoomSettings = new ArrayList<>();

    //채팅 관련
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Message> messages= new ArrayList<>();

    //신고 관련
    @Column(columnDefinition = "integer default 0")
    @Builder.Default
    private Integer reportCount = 0;

    @Column(columnDefinition = "boolean default false")
    private boolean isBlacklisted=false;

    private LocalDateTime blacklistedAt;

    //신고 횟수 증가
    public void increaseReportCount() {
        if (this.reportCount == null) {
            this.reportCount = 0;
        }
        this.reportCount++;
    }

    public void decreaseReportCount() {
        this.reportCount--;
    }

    // 블랙리스트 처리
    public void blacklist() {
        this.isBlacklisted = true;
        this.blacklistedAt = LocalDateTime.now();
    }

    // 유저 정보 관련
    public void updateEducation(Education education) {
        this.education = education;
    }
    public void updateEmployment(Employment employment) {
        this.employment = employment;
    }
    public void updateIncome(Income income) {
        this.income = income;
    }
    public void updateRegion(Region region) { this.region = region;}
    public void updateIsDeleted(Boolean isDeleted){this.isDeleted = isDeleted;}
    public void updateRewardLevel(RewardLevel reward) {this.reward = reward;}
    public void updatePassword(String newPassword) {this.password = newPassword;}
    public void updateName(String newName){this.name = newName;}

    // 알람 관련
    public void updateNotificationSetting(NotificationSetting notificationSetting) {this.notificationSetting = notificationSetting;}

    @PostPersist
    public void postPersistInitialization() {
        // 알람 초기화
        if (this.notificationSetting == null) {
            this.notificationSetting = NotificationSetting.builder()
                    .user(this)
                    .build();
        }
        // 캐릭터 리스트 초기화
        if (characters == null || characters.isEmpty()) {
            this.characters = Arrays.stream(RewardLevel.values())
                    .map(level -> Character.builder()
                            .user(this)
                            .rewardLevel(level)
                            .open(level == RewardLevel.LEVEL_1)
                            .build())
                    .toList();
        }
    }
}