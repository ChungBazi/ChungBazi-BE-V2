package chungbazi.chungbazi_be.domain.notification.repository;

import chungbazi.chungbazi_be.domain.notification.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByUserId(Long userId);

    Optional<FcmToken> findByToken(String token);
}
