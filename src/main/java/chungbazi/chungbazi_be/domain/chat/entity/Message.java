package chungbazi.chungbazi_be.domain.chat.entity;

import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Message extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ChatRoom chatRoom;

    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false;

    public void markAsRead() {
        this.isRead = true;
    }
}
