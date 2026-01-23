package chungbazi.chungbazi_be.domain.user.repository;

import chungbazi.chungbazi_be.domain.user.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<User> findByName(String name);
    boolean existsByName(String name);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.name = '(알 수 없음)', u.email = CONCAT('deleted_', u.id, '@chungbazi.com') WHERE u.id = :userId")
    void anonymizeUser(@Param("userId") Long userId);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.userAdditionList ua " +
            "LEFT JOIN FETCH ua.addition " +
            "WHERE u.id = :userId")
    Optional<User> findByIdWithAdditions(@Param("userId") Long userId);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.userInterestList ui " +
            "LEFT JOIN FETCH ui.interest " +
            "WHERE u.id = :userId")
    Optional<User> findByIdWithInterests(@Param("userId") Long userId);
}