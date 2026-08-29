package com.chungbazi.server.domain.notification.domain.repository;

import com.chungbazi.server.domain.notification.domain.Notification;
import com.chungbazi.server.domain.notification.domain.type.NotificationCategory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.chungbazi.server.domain.notification.domain.QNotification.notification;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryCustomImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Notification> findNotifications(
            Long userId,
            NotificationCategory category,
            Long cursor,
            Pageable pageable
    ) {
        return queryFactory
                .selectFrom(notification)
                .where(
                        notification.userId.eq(userId),
                        categoryEq(category),
                        cursorLt(cursor)
                )
                .orderBy(notification.id.desc())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression categoryEq(NotificationCategory category) {
        return category == null ? null : notification.category.eq(category);
    }

    private BooleanExpression cursorLt(Long cursor) {

        return cursor == null ? null : notification.id.lt(cursor);
    }
}
