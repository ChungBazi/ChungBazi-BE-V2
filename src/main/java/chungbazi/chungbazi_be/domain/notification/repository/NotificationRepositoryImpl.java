package chungbazi.chungbazi_be.domain.notification.repository;

import chungbazi.chungbazi_be.domain.notification.dto.NotificationResponseDTO;
import chungbazi.chungbazi_be.domain.notification.entity.QNotification;
import chungbazi.chungbazi_be.domain.notification.entity.enums.NotificationType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom{

    private final JPAQueryFactory queryFactory;


    //알림 읽음 처리
    @Override
    public void markAllAsRead(Long userId, NotificationType type){

        QNotification qNotification=QNotification.notification;

        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(qNotification.user.id.eq(userId));
        booleanBuilder.and(qNotification.isRead.eq(false));

        if (type!=null){
            booleanBuilder.and(qNotification.type.eq(type));
        }

        queryFactory.update(qNotification)
                .set(qNotification.isRead,true)
                .where(booleanBuilder)
                .execute();
    }

    //알림 조회
    @Override
    public List<NotificationResponseDTO.notificationsDto> findNotificationsByUserIdAndNotificationTypeDto(
            Long userId, NotificationType type, Long cursor, int limit) {

        QNotification qNotification = QNotification.notification;

        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(qNotification.user.id.eq(userId));

        if (type != null) {
            booleanBuilder.and(qNotification.type.eq(type));
        }
        if (cursor != null && cursor != 0) {
            booleanBuilder.and(qNotification.id.lt(cursor));
        }

        List<NotificationResponseDTO.notificationsDto> dtos = queryFactory
                .select(Projections.constructor(
                        NotificationResponseDTO.notificationsDto.class,
                        qNotification.id,
                        qNotification.isRead,
                        qNotification.message,
                        qNotification.type,
                        qNotification.targetId,
                        qNotification.createdAt
                ))
                .from(qNotification)
                .where(booleanBuilder)
                .orderBy(qNotification.createdAt.desc())
                .limit(limit + 1)
                .fetch();

        return dtos;
    }

}
