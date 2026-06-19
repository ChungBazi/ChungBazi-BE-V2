package com.chungbazi.server.domain.user.domain;

import com.chungbazi.server.domain.policy.enums.PolicyCategoryType;
import com.chungbazi.server.domain.policy.enums.PolicySubCategoryType;
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
        name = "user_interest",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_interest_user_sub_category",
                columnNames = {"user_id", "sub_category"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_interest_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private PolicyCategoryType category;

    @Enumerated(EnumType.STRING)
    @Column(name = "sub_category", nullable = false, length = 40)
    private PolicySubCategoryType subCategory;

    public static UserInterest createUserInterest(
            User user,
            PolicySubCategoryType subCategory
    ) {
        if (subCategory == null) {
            throw new IllegalArgumentException("관심분야 중분류 선택은 필수입니다.");
        }
        UserInterest userInterest = new UserInterest();
        userInterest.user = user;
        userInterest.category = subCategory.getCategory();
        userInterest.subCategory = subCategory;

        return userInterest;
    }
}
