package chungbazi.chungbazi_be.domain.user.entity;

import chungbazi.chungbazi_be.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_blocker_blocked",
                        columnNames = {"blocker_id", "blocked_id"}
                )
        },
        indexes = {
                @Index(name = "idx_blocker_id_is_active", columnList = "blocker_id, is_active")
        }
)
public class UserBlock extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}

