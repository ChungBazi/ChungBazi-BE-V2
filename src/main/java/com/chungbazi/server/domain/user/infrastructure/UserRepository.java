package com.chungbazi.server.domain.user.infrastructure;

import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.type.SocialType;
import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.LockModeType;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialTypeAndProviderId(SocialType socialType, String providerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT user
        FROM User user
        WHERE user.id = :userId
    """)
    Optional<User> findByIdForUpdate(@Param("userId") Long userId);

    @Query("""
            SELECT user
            FROM User user
            WHERE user.id > :cursor
              AND user.deleted = false
              AND user.onboardingCompleted = true
            ORDER BY user.id ASC
            """)
    List<User> findNotificationTargetUsersAfterId(
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    @Query("""
            SELECT user
            FROM User user
            WHERE user.id > :cursor
              AND user.deleted = false
              AND user.onboardingCompleted = true
              AND EXISTS (
                    SELECT userInterest.id
                    FROM UserInterest userInterest
                    WHERE userInterest.user = user
                      AND userInterest.subCategory IN :subCategories
              )
            ORDER BY user.id ASC
            """)
    List<User> findInterestNotificationTargetUsersAfterId(
            @Param("subCategories") Collection<PolicySubCategoryType> subCategories,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

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

    @Transactional
    @Modifying
    @Query("""
            update User user
            set user.fcmToken = null
            where user.fcmToken in :fcmTokens
            """)
    int clearInvalidFcmTokens(@Param("fcmTokens") Collection<String> fcmTokens);
}
