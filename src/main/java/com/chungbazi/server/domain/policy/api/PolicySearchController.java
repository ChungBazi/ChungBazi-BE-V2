package com.chungbazi.server.domain.policy.api;

import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse;
import com.chungbazi.server.domain.policy.api.dto.response.RecentSearchPolicyListResponse;
import com.chungbazi.server.domain.policy.application.PolicySearchService;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.resolver.CurrentUser;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/policies")
public class PolicySearchController {

    private final PolicySearchService policySearchService;

    @GetMapping("/search")
    public CommonResponse<PolicyListResponse> searchPolicies(
            @CurrentUser User user,
            @RequestParam String keyword,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return CommonResponse.onSuccess(policySearchService.searchPolicies(user, keyword, cursor, size));
    }

    @GetMapping("/recent-search")
    public CommonResponse<RecentSearchPolicyListResponse> getRecentSearchPolicies(@CurrentUser User user) {
        return CommonResponse.onSuccess(policySearchService.getRecentSearchPolicies(user));
    }
}
