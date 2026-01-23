package chungbazi.chungbazi_be.domain.notification.entity;

import chungbazi.chungbazi_be.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String token;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "timestamp")
    private LocalDateTime lastUsedAt;

    public void updateLastUsedAt() {
        this.lastUsedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return lastUsedAt.isBefore(LocalDateTime.now().minusDays(30));
    }

    public void updateToken(String newToken) {
        this.lastUsedAt = LocalDateTime.now();
        this.token = newToken;
    }
}
