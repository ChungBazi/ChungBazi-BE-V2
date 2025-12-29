package chungbazi.chungbazi_be.domain.chat.service;

import chungbazi.chungbazi_be.domain.chat.entity.ChatRoom;
import chungbazi.chungbazi_be.domain.chat.entity.ChatRoomSetting;
import chungbazi.chungbazi_be.domain.chat.repository.ChatRoomSettingRepository;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.utils.UserHelper;
import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.NotFoundHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRoomSettingService {
    private final ChatRoomSettingRepository chatRoomSettingRepository;
    private final UserHelper userHelper;
    private final ChatRoomService chatRoomService;

    public boolean getChatRoomSettingIsEnabled(Long userId, Long roomId) {
        ChatRoomSetting chatRoomSetting= chatRoomSettingRepository.findByUserIdAndChatRoomId(userId,roomId)
                .orElseThrow(()->new NotFoundHandler(ErrorStatus.NOT_FOUND_CHATROOM_SETTING));

        return chatRoomSetting.isEnabled();
    }

    public void setChatRoomSettingIsEnabled(Long chatRoomId, boolean enabled) {
        User user = userHelper.getAuthenticatedUser();

        ChatRoom chatRoom = chatRoomService.getChatRoomById(chatRoomId);

        ChatRoomSetting chatRoomSetting= chatRoomSettingRepository.findByUserIdAndChatRoomId(user.getId(),chatRoomId)
                .orElseThrow(()->new NotFoundHandler(ErrorStatus.NOT_FOUND_CHATROOM_SETTING));

        chatRoomSetting.updateChatNotification(enabled);
        chatRoomSettingRepository.save(chatRoomSetting);
    }

}
