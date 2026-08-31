package com.chungbazi.server.domain.policy.api;

import com.chungbazi.server.domain.policy.api.docs.PolicyDetailDocs;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyCardListResponse;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyCardResponse;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyDetailResponse;
import com.chungbazi.server.domain.policy.application.PolicyDetailService;
import com.chungbazi.server.domain.policy.application.PolicyLikeService;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.resolver.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/policies")
public class PolicyDetailController implements PolicyDetailDocs {

    private final PolicyDetailService policyDetailService;
    private final PolicyLikeService policyLikeService;

    @Override
    @GetMapping("/cards")
    public CommonResponse<PolicyCardListResponse> getPolicyCards(
            @CurrentUser User user,
            @RequestParam(required = false) PolicyCategoryType category
    ) {
        return CommonResponse.onSuccess(policyDetailService.getPolicyCards(user, category));
    }

    @Override
    @GetMapping("/card/{policyId}")
    public CommonResponse<PolicyCardResponse> getPolicyCard(
            @CurrentUser User user,
            @PathVariable Long policyId
    ) {
        return CommonResponse.onSuccess(policyDetailService.getPolicyCard(user, policyId));
    }

    @Override
    @GetMapping("/detail/{policyId}")
    public CommonResponse<PolicyDetailResponse> getPolicyDetail(
            @CurrentUser User user,
            @PathVariable Long policyId
    ) {
        return CommonResponse.onSuccess(policyDetailService.getPolicyDetail(user, policyId));
    }

    @Override
    @PostMapping("/{policyId}/like")
    public CommonResponse<String> likePolicy(
            @CurrentUser User user,
            @PathVariable Long policyId
    ) {
        policyLikeService.likePolicy(user, policyId);
        return CommonResponse.onSuccess("정책 찜이 완료되었습니다.");
    }

    @Override
    @DeleteMapping("/{policyId}/like")
    public CommonResponse<String> unlikePolicy(
            @CurrentUser User user,
            @PathVariable Long policyId
    ) {
        policyLikeService.unlikePolicy(user, policyId);
        return CommonResponse.onSuccess("정책 찜 취소가 완료되었습니다.");
    }
}
