package chungbazi.chungbazi_be.domain.chat.controller;

import chungbazi.chungbazi_be.domain.chat.dto.ChatRequestDTO;
import chungbazi.chungbazi_be.domain.chat.dto.ChatResponseDTO;
import chungbazi.chungbazi_be.domain.chat.service.ChatService;
import chungbazi.chungbazi_be.domain.chat.service.ChatRoomSettingService;
import chungbazi.chungbazi_be.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final ChatRoomSettingService chatRoomSettingService;

    @PostMapping("/chat/{postId}/create-room")
    @Operation(summary = "채팅방 생성 API", description = "채팅방을 생성하는 API입니다.")
    public ChatResponseDTO.createChatRoomResponse createChatRoom(@PathVariable Long postId, @RequestParam Long receiverId){
        return chatService.createChatRoom(postId,receiverId);
    }

    @MessageMapping("/chat.message.{chatRoomId}")
    public void sendMessage(@DestinationVariable Long chatRoomId, ChatRequestDTO.MessageDto dto) {
        log.info("sendMessage: chatRoomId={}, message={}", chatRoomId, dto.getContent());
        chatService.sendMessage(chatRoomId, dto);

    }

    @GetMapping("/chatRooms/{chatRoomId}")
    @Operation(summary = "채팅방 상세 조회",description = "채팅방의 상세 정보와 메세지들을 조회합니다.")
    public ApiResponse<ChatResponseDTO.chatRoomResponse> getChatRoomDetail(@PathVariable Long chatRoomId, @RequestParam(required = false) Long cursorId, @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.onSuccess(chatService.getChatRoomDetail(chatRoomId,cursorId,limit));
    }

    @DeleteMapping("/chatRooms/{chatRoomId}/leave")
    @Operation(summary = "채팅방 나가기", description = "채팅방에서 나가는 API입니다.")
    public ApiResponse<Void> leaveChatRoom(@PathVariable Long chatRoomId) {
        chatService.leaveChatRoom(chatRoomId);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/chatRooms")
    @Operation(summary = "채팅방 목록 조회", description = """
            유저의 채팅방 목록을 조회하는 API입니다.
            isBlocked == true인 경우, 차단된 채팅방 목록을,
            isBlocked == false인 경우, 활성화된 채팅방 목록을 반환합니다.
            """)
    public ApiResponse<List<ChatResponseDTO.chatRoomListResponse>> getChatRooms(@RequestParam(required = false) boolean isBlocked) {
        return ApiResponse.onSuccess(chatService.getChatRoomList(isBlocked));
    }

    @PatchMapping("/chatRooms/{chatRoomId}/setting")
    @Operation(summary = "채팅방 알림 설정", description = "채팅방 알림을 설정하는 API입니다.")
    public ApiResponse<Void> setChatRoomSetting(@PathVariable Long chatRoomId, @RequestParam(required = false) boolean enabled) {
        chatRoomSettingService.setChatRoomSettingIsEnabled(chatRoomId, enabled);
        return ApiResponse.onSuccess(null);
    }
}
