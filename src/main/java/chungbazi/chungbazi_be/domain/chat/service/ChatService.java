package chungbazi.chungbazi_be.domain.chat.service;

import chungbazi.chungbazi_be.domain.chat.converter.ChatConverter;
import chungbazi.chungbazi_be.domain.chat.dto.ChatRequestDTO;
import chungbazi.chungbazi_be.domain.chat.dto.ChatResponseDTO;
import chungbazi.chungbazi_be.domain.chat.entity.ChatRoom;
import chungbazi.chungbazi_be.domain.chat.entity.Message;
import chungbazi.chungbazi_be.domain.chat.repository.chatRoom.ChatRoomRepository;
import chungbazi.chungbazi_be.domain.chat.repository.MessageRepository.MessageRepository;
import chungbazi.chungbazi_be.domain.community.entity.Post;
import chungbazi.chungbazi_be.domain.community.repository.PostRepository;
import chungbazi.chungbazi_be.domain.notification.dto.internal.NotificationData;
import chungbazi.chungbazi_be.domain.chat.entity.ChatRoomSetting;
import chungbazi.chungbazi_be.domain.notification.entity.enums.NotificationType;
import chungbazi.chungbazi_be.domain.notification.service.NotificationService;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.repository.UserBlockRepository.UserBlockRepository;
import chungbazi.chungbazi_be.domain.user.repository.UserRepository;
import chungbazi.chungbazi_be.domain.user.service.UserBlockService;
import chungbazi.chungbazi_be.domain.user.support.UserHelper;
import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import chungbazi.chungbazi_be.global.apiPayload.exception.GeneralException;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.BadRequestHandler;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.NotFoundHandler;
import chungbazi.chungbazi_be.global.utils.PaginationResult;
import chungbazi.chungbazi_be.global.utils.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final UserHelper userHelper;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final UserBlockRepository userBlockRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserBlockService userBlockService;
    private final NotificationService notificationService;
    private final ChatRoomSettingService chatRoomSettingService;

    @Transactional
    public ChatResponseDTO.createChatRoomResponse createChatRoom(Long postId, Long receiverId){
        User sender = userHelper.getAuthenticatedUser();

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(()-> new NotFoundHandler(ErrorStatus.NOT_FOUND_USER));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_POST));

        if (sender.getId().equals(receiverId)){
            throw new BadRequestHandler(ErrorStatus.CAN_NOT_CHATTING_MYSELF);
        }

        ChatRoom chatRoom=ChatRoom.builder()
                .post(post)
                .isActive(true)
                .build();

        List<User> participants = List.of(sender, receiver);
        participants.forEach(user -> {
            ChatRoomSetting setting = ChatRoomSetting.builder()
                    .user(user)
                    .chatRoom(chatRoom)
                    .enabled(true)
                    .build();
            chatRoom.getChatRoomSettings().add(setting);
        });
        chatRoomRepository.save(chatRoom);

        return ChatConverter.toChatRoomDTO(chatRoom,sender.getId(), post.getAuthor().getId());
    }

    @Transactional
    public ChatResponseDTO.messageResponse sendMessage(Long chatRoomId, ChatRequestDTO.MessageDto dto){
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_CHATROOM));

        User sender = userHelper.getAuthenticatedUser();

        if (!chatRoom.isParticipant(sender)) {
            throw new GeneralException(ErrorStatus.ACCESS_DENIED_CHATROOM);
        }

        if (!getOtherUser(chatRoom, sender).getId().equals(dto.getReceiverId())) {
            throw new GeneralException(ErrorStatus.ACCESS_DENIED_CHATROOM);
        }

        if (userBlockRepository.existsBlockBetweenUsers(sender.getId(), dto.getReceiverId())){
            throw new GeneralException(ErrorStatus.BLOCKED_CHATROOM);
        }

        Message message = Message.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(dto.getContent())
                .isRead(false)
                .build();

        messageRepository.save(message);
        sendChatNotification(dto.getReceiverId(),message);

        ChatResponseDTO.messageResponse response=ChatResponseDTO.messageResponse.builder()
                .id(message.getId())
                .receiverId(dto.getReceiverId())
                .senderId(message.getSender().getId())
                .createdAt(message.getCreatedAt())
                .chatRoomId(chatRoomId)
                .content(dto.getContent())
                .isRead(message.isRead())
                .build();

        simpMessagingTemplate.convertAndSend("/topic/chat.room." + chatRoomId, response);

        return response;
    }

    @Transactional
    public void sendChatNotification(Long receiverId, Message chat){
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_USER));
        if(chatRoomSettingService.getChatRoomSettingIsEnabled(receiverId,chat.getChatRoom().getId())){
            String message = chat.getSender().getName() + "님이 쪽지를 보내셨습니다.";

            NotificationData request = NotificationData.builder()
                    .user(receiver)
                    .type(NotificationType.CHAT)
                    .message(message)
                    .targetId(chat.getId())
                    .build();

            notificationService.sendNotification(request);
        }
    }

    @Transactional
    public ChatResponseDTO.chatRoomResponse getChatRoomDetail(Long chatRoomId,Long cursor, int limit){
        ChatRoom chatRoom=chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_CHATROOM));

        if (!chatRoom.isParticipant(userHelper.getAuthenticatedUser())){
            throw new GeneralException(ErrorStatus.ACCESS_DENIED_CHATROOM);
        }

        User currentUser = userHelper.getAuthenticatedUser();

        User other = getOtherUser(chatRoom,currentUser);

        boolean chatRoomIsEnabled= chatRoomSettingService.getChatRoomSettingIsEnabled(currentUser.getId(), chatRoom.getId());


        if (userBlockRepository.existsBlockBetweenUsers(currentUser.getId(), other.getId())){
            throw new GeneralException(ErrorStatus.BLOCKED_CHATROOM);
        }


        markMessagesAsRead(chatRoom,currentUser);

        List<Message> messages = messageRepository.findMessagesByChatRoomId(chatRoomId,cursor,limit+1);
        PaginationResult<Message> paginationResult = PaginationUtil.paginate(messages,limit);

        List<ChatResponseDTO.chatDetailMessage> messageList = paginationResult.getItems().stream()
                .map(message -> ChatConverter.toChatDetailMessageDTO(message, currentUser.getId()))
                .collect(Collectors.toList());

        return ChatResponseDTO.chatRoomResponse.builder()
                .chatRoomId(chatRoomId)
                .postTitle(chatRoom.getPost().getTitle())
                .chatRoomNotification(chatRoomIsEnabled)
                .messageList(messageList)
                .receiverId(other.getId())
                .receiverName(other.getName())
                .build();
    }

    public void markMessagesAsRead(ChatRoom chatRoom, User currentUser){

        long updatedCount = messageRepository.markAllAsRead(chatRoom.getId(),currentUser.getId());

        if (updatedCount>0){
            User receiver = getOtherUser(chatRoom,currentUser);

            simpMessagingTemplate.convertAndSendToUser(
                    receiver.getId().toString(),
                    "/queue/message-read",
                    Map.of("chatRoomId",chatRoom.getId(),"readCount",updatedCount));
        }
    }

    public void leaveChatRoom(Long charRoomId){
        ChatRoom chatRoom = chatRoomRepository.findById(charRoomId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_CHATROOM));

        User currentUser = userHelper.getAuthenticatedUser();

        if (!chatRoom.isParticipant(currentUser)){
            throw new GeneralException(ErrorStatus.ACCESS_DENIED_CHATROOM);
        }

        chatRoom.setIsActive(false);
        chatRoomRepository.save(chatRoom);

        User receiver = getOtherUser(chatRoom,userHelper.getAuthenticatedUser());

        simpMessagingTemplate.convertAndSendToUser(
                receiver.getId().toString(),
                "/queue/chat-room-left",
                Map.of("chatRoomId",charRoomId,"message", "상대방이 채팅방을 나갔습니다")
        );
    }

    public List<ChatResponseDTO.chatRoomListResponse> getChatRoomList(boolean isBlocked){
        User user=userHelper.getAuthenticatedUser();
        List<ChatRoom> chatRooms;

        if (!isBlocked){
            chatRooms = chatRoomRepository.findActiveRoomsByUserId(user.getId()).stream()
                            .filter(chatRoom -> {
                                Long otherUserId = getOtherUser(chatRoom,user).getId();
                            return !userBlockService.isUserBlocked(otherUserId);
                            })
                    .collect(Collectors.toList());
        }else {
            chatRooms = chatRoomRepository.findBlockedChatRoomsByUserId(user.getId());

        }
        List<ChatResponseDTO.chatRoomListResponse> chatRoomListResponses = chatRooms.stream()
                .map(chatRoom -> {
                    Message lastMessage = messageRepository.findLastMessageByChatRoomId(chatRoom.getId())
                            .orElse(null);
                            //.orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_CHATROOM));
                    return ChatConverter.toChatRoomListResponse(chatRoom, lastMessage,getOtherUser(chatRoom,user));
                })
                .collect(Collectors.toList());

        return chatRoomListResponses;
    }

    public User getOtherUser(ChatRoom chatRoom, User currentUser) {
        return chatRoom.getChatRoomSettings().stream()
                .map(ChatRoomSetting::getUser)
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_CHATROOM));
    }
}
