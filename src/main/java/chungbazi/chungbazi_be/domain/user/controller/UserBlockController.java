package chungbazi.chungbazi_be.domain.user.controller;

import chungbazi.chungbazi_be.domain.user.service.UserBlockService;
import chungbazi.chungbazi_be.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/block/{blockedUserId}")
public class UserBlockController {
    private final UserBlockService userBlockService;

    @PostMapping
    @Operation(summary = "유저 차단 API", description = "특정 유저를 차단하는 API입니다.")
    public ApiResponse<String> blockUser(@PathVariable Long blockedUserId){
        userBlockService.blockUser(blockedUserId);
        return ApiResponse.onSuccess("사용자 차단이 성공적으로 실행되었습니다.");
    }

    @PatchMapping
    @Operation(summary = "유저 차단 해제 API", description = "특정 유저의 차단을 해제하는 API입니다.")
    public ApiResponse<String> unblockUser(@PathVariable Long blockedUserId){
        userBlockService.unblockUser(blockedUserId);
        return ApiResponse.onSuccess("사용자 차단 해제가 성공적으로 실행되었습니다.");
    }
}
