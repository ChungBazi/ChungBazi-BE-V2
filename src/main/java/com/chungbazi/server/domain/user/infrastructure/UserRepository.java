package com.chungbazi.server.domain.user.infrastructure;

import com.chungbazi.server.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
