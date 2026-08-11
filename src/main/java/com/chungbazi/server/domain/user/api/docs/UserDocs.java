package com.chungbazi.server.domain.user.api.docs;

import com.chungbazi.server.domain.user.api.dto.request.UserNameRequest;
import com.chungbazi.server.domain.user.api.dto.request.UserOnboardingRequest;
import com.chungbazi.server.domain.user.api.dto.request.UserPolicyRequest;
import com.chungbazi.server.domain.user.api.dto.request.UserWithdrawalRequest;
import com.chungbazi.server.domain.user.api.dto.response.UserInfoResponse;
import com.chungbazi.server.domain.user.api.dto.response.UserPolicyResponse;
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

    @Operation(
            summary = "사용자 정보 조회 API",
            description = """
                ### ResponseBody
                ---
                - `name`: 사용자 이름
                - `email`: 사용자 이메일
                - `socialType`: 소셜 로그인 타입
                """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자 정보가 성공적으로 조회됐습니다."
            )
    })
    CommonResponse<UserInfoResponse> getUserInfo(@CurrentUser User user);

    @Operation(
            summary = "정책 추천 기준 조회 API",
            description = """
                ### ResponseBody
                ---
                - `birth`: 생년월일
                - `sidoCode`: 거주 지역 시/도 코드
                - `sigunguCode`: 거주 지역 시/군/구 코드
                - `educationCode`: 최종 학력 코드
                - `employmentCode`: 현재 취업 상태 코드
                - `incomeLevel`: 소득 구간
                - `interestCategories`: 관심 정책 분야 목록
                """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "정책 추천 기준이 성공적으로 조회됐습니다."
            )
    })
    CommonResponse<UserPolicyResponse> getUserPolicy(@CurrentUser User user);

    @Operation(
            summary = "회원 탈퇴 API",
            description = """
                    ### RequestBody
                    ---
                    - `reasons`: 탈퇴 사유 목록. 한 개 이상 선택 필수
                         - `POLICY_DISCOVERY_DIFFICULT`: 원하는 정책을 찾기 어려워요
                         - `INSUFFICIENT_POLICY_INFORMATION`: 저에게 맞는 정책 추천이 부족해요
                         - `NO_LONGER_NEEDED`: 이용할 일이 없어졌어요
                         - `INCONVENIENT_APP`: 앱 사용이 불편했어요
                         - `FREQUENT_ERRORS`: 오류가 자주 발생했어요
                         - `OTHER`: 기타 이유가 있어요
                    - `detail`: 기타 불편 사항 또는 상세 의견. 선택 입력
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "회원 탈퇴가 성공적으로 완료되었습니다."
            )
    })
    CommonResponse<String> withdrawUser(
            @CurrentUser User user,
            @Valid @RequestBody UserWithdrawalRequest request
    );
}
