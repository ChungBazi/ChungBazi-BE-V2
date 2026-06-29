package com.chungbazi.server.domain.user.api.docs;

import com.chungbazi.server.domain.user.api.dto.UserNameRequest;
import com.chungbazi.server.domain.user.api.dto.UserOnboardingRequest;
import com.chungbazi.server.domain.user.api.dto.UserPolicyRequest;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.resolver.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "[User]", description = "사용자 관련 API")
public interface UserDocs {
    @Operation(
            summary = "온보딩 API",
            description = """
                    ### RequestBody
                    ---
                    - `name`: 사용자 이름
                    - `birth`: 생년월일
                    - `sidoCode`: 거주 지역 시/도 코드
                    - `sigunguCode`: 거주 지역 시/군/구 코드
                    - `educationCode`: 최종 학력 코드
                    - `employmentCode`: 현재 취업 상태 코드
                    - `incomeLevel`: 소득 구간
                    - `interestCategories`: 관심 정책 분야 목록. 3개 이상 선택 필수
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "온보딩이 성공적으로 완료됐습니다."
            )
    })
    CommonResponse<String> saveUserOnboarding(
            @CurrentUser User user,
            @Valid @RequestBody UserOnboardingRequest request
    );

    @Operation(
            summary = "사용자 이름 수정 API",
            description = """
                    ### RequestBody
                    ---
                    - `name`: 수정할 사용자 이름
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 이름이 성공적으로 수정됐습니다."
            )
    })
    CommonResponse<String> updateUserName(
            @CurrentUser User user,
            @Valid @RequestBody UserNameRequest request
    );

    @Operation(
            summary = "정책 추천 기준 수정 API",
            description = """
                    ### RequestBody
                    ---
                    - `birth`: 생년월일
                    - `sidoCode`: 거주 지역 시/도 코드
                    - `sigunguCode`: 거주 지역 시/군/구 코드
                    - `educationCode`: 최종 학력 코드
                    - `employmentCode`: 현재 취업 상태 코드
                    - `incomeLevel`: 소득 구간
                    - `interestCategories`: 관심 정책 분야 목록. 3개 이상 선택 필수
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "정책 추천 기준이 성공적으로 수정됐습니다."
            )
    })
    CommonResponse<String> updateUserPolicy(
            @CurrentUser User user,
            @Valid @RequestBody UserPolicyRequest request
    );
}
