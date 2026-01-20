package chungbazi.chungbazi_be.global.utils;

import chungbazi.chungbazi_be.domain.chat.entity.Message;
import chungbazi.chungbazi_be.domain.community.entity.Comment;
import chungbazi.chungbazi_be.domain.community.entity.Post;
import chungbazi.chungbazi_be.domain.notification.dto.response.NotificationResponseDTO;
import chungbazi.chungbazi_be.domain.notification.entity.Notification;
import chungbazi.chungbazi_be.domain.policy.dto.PolicyListOneResponse;

import java.util.List;

public class PaginationUtil {
    public static <T> PaginationResult<T> paginate(List<T> items, int size) {
        boolean hasNext = items.size() > size;
        Long nextCursor = 0L;

        if (hasNext) {
            T lastItem = items.get(size - 1);

            if (lastItem instanceof Post) {
                nextCursor = ((Post) lastItem).getId();
            } else if (lastItem instanceof Comment) {
                nextCursor = ((Comment) lastItem).getId();
            } else if (lastItem instanceof Notification) {
                nextCursor = ((Notification) lastItem).getId();
            } else if (lastItem instanceof Message) {
                nextCursor = ((Message) lastItem).getId();
            } else if (lastItem instanceof NotificationResponseDTO.notificationsDto) {
                nextCursor = ((NotificationResponseDTO.notificationsDto) lastItem).getNotificationId();
            } else if (lastItem instanceof PolicyListOneResponse){
                nextCursor = ((PolicyListOneResponse) lastItem).getPolicyId();
            } else {
                throw new IllegalArgumentException("Unsupported entity type for pagination");
            }

            items = items.subList(0, size);
        }
        return new  PaginationResult<>(items, nextCursor, hasNext);
    }
}
