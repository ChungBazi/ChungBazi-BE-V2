package com.chungbazi.server.domain.policy.api;

import com.chungbazi.server.domain.policy.api.docs.HomeDocs;
import com.chungbazi.server.domain.policy.api.dto.response.HomePolicyResponse;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse;
import com.chungbazi.server.domain.policy.application.HomePolicyService;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.PolicySortType;
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
@RequestMapping("/v1/home")
public class HomeController implements HomeDocs {

    private final HomePolicyService homePolicyService;

    @Override
    @GetMapping
    public CommonResponse<HomePolicyResponse> getHomePolicies(@CurrentUser User user) {
        return CommonResponse.onSuccess(
                homePolicyService.getHomePolicies(user)
        );
    }

    @Override
    @GetMapping("/policies/recent-viewed")
    public CommonResponse<PolicyListResponse> getRecentViewedPolicies(
            @CurrentUser User user,
            @RequestParam(defaultValue = "5") @Min(1) @Max(5) int size
    ) {
        return CommonResponse.onSuccess(
                homePolicyService.getRecentViewedPolicies(user, size)
        );
    }

    @Override
    @GetMapping("/policies")
    public CommonResponse<PolicyListResponse> getPoliciesByCategory(
            @CurrentUser User user,
            @RequestParam PolicyCategoryType category,
            @RequestParam(defaultValue = "LATEST") PolicySortType sort,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return CommonResponse.onSuccess(
                homePolicyService.getPolicies(user, category, sort, cursor, size)
        );
    }

    @Override
    @GetMapping("/policies/latest")
    public CommonResponse<PolicyListResponse> getLatestPolicies(
            @CurrentUser User user,
            @RequestParam(required = false) PolicyCategoryType category,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return CommonResponse.onSuccess(
                homePolicyService.getLatestPolicies(user, category, cursor, size)
        );
    }

    @Override
    @GetMapping("/policies/deadline")
    public CommonResponse<PolicyListResponse> getUpcomingDeadlinePolicies(
            @CurrentUser User user,
            @RequestParam(required = false) PolicyCategoryType category,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return CommonResponse.onSuccess(
                homePolicyService.getUpcomingDeadlinePolicies(user, category, cursor, size)
        );
    }

    @Override
    @GetMapping("/policies/popular")
    public CommonResponse<PolicyListResponse> getPopularPolicies(
            @CurrentUser User user,
            @RequestParam(required = false) PolicyCategoryType category,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return CommonResponse.onSuccess(
                homePolicyService.getPopularPolicies(user, category, cursor, size)
        );
    }
}
