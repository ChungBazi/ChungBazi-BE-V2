package com.chungbazi.server.fixture;

import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.type.IncomeLevel;
import com.chungbazi.server.domain.user.domain.type.SocialType;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.ZoneId;

public final class UserFixture {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    private UserFixture() {
    }

    public static UserBuilder user() {
        return new UserBuilder();
    }

    public static final class UserBuilder {

        private Long id = 1L;
        private int age = 25;
        private IncomeLevel incomeLevel = IncomeLevel.UNKNOWN;

        public UserBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder age(int age) {
            this.age = age;
            return this;
        }

        public UserBuilder incomeLevel(IncomeLevel incomeLevel) {
            this.incomeLevel = incomeLevel;
            return this;
        }

        public User build() {
            User user = User.create(
                    "test-provider-" + id,
                    SocialType.KAKAO,
                    "test-" + id + "@example.com",
                    "테스트 사용자 " + id,
                    null
            );

            LocalDate birth = LocalDate.now(SERVICE_ZONE_ID).minusYears(age);
            user.saveUserOnboarding(
                    birth.toString(),
                    null,
                    null,
                    null,
                    null,
                    incomeLevel
            );
            ReflectionTestUtils.setField(user, "id", id);
            return user;
        }
    }
}
