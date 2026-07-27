package com.chungbazi.server.domain.policy.api;

import com.chungbazi.server.domain.policy.api.docs.PolicyDetailDocs;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyCardResponse;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyDetailResponse;
import com.chungbazi.server.domain.policy.application.PolicyDetailService;
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

    @Override
    @GetMapping("/card")
    public CommonResponse<PolicyCardResponse> getPolicyCard(
            @CurrentUser User user,
            @PathVariable Long policyId
    ) {
        return CommonResponse.onSuccess(policyDetailService.getPolicyCard(user, policyId));
    }

    @Override
    @GetMapping("/detail")
    public CommonResponse<PolicyDetailResponse> getPolicyDetail(
            @CurrentUser User user,
            @PathVariable Long policyId
    ) {
        return CommonResponse.onSuccess(policyDetailService.getPolicyDetail(user, policyId));
    }
}
