package com.chungbazi.server.domain.user.infrastructure;

import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.UserInterest;
import com.chungbazi.server.domain.policy.domain.type.PolicySubCategoryType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {
    List<UserInterest> findAllByUser(User user);

    @Query("""
            SELECT userInterest
            FROM UserInterest userInterest
            JOIN FETCH userInterest.user user
            WHERE user.id IN :userIds
            """)
    List<UserInterest> findAllByUserIds(
            @Param("userIds") Collection<Long> userIds
    );

    @Query("""
            SELECT userInterest
            FROM UserInterest userInterest
            JOIN FETCH userInterest.user user
            WHERE user.id IN :userIds
              AND userInterest.subCategory IN :subCategories
            """)
    List<UserInterest> findAllByUserIdsAndSubCategories(
            @Param("userIds") Collection<Long> userIds,
            @Param("subCategories") Collection<PolicySubCategoryType> subCategories
    );

    void deleteAllByUserId(Long userId);
}
