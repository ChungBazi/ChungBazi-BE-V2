package com.chungbazi.server.domain.user.infrastructure;

import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.type.SocialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialTypeAndProviderId(SocialType socialType, String providerId);

    @Modifying(flushAutomatically = true)
    @Query("""
            update User user
            set user.fcmToken = null
            where user.fcmToken = :fcmToken
              and user.id <> :currentUserId
            """)
    int clearFcmTokenFromOtherUsers(
            @Param("fcmToken") String fcmToken,
            @Param("currentUserId") Long currentUserId
    );
}
