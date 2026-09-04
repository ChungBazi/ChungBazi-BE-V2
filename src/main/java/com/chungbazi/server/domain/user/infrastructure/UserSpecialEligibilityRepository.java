package com.chungbazi.server.domain.user.infrastructure;

import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.domain.UserSpecialEligibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSpecialEligibilityRepository extends JpaRepository<UserSpecialEligibility, Long> {
    List<UserSpecialEligibility> findAllByUser(User user);
    void deleteAllByUserId(Long userId);
}
