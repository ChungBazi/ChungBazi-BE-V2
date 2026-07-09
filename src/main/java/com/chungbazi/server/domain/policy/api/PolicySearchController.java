package com.chungbazi.server.domain.policy.api;

import com.chungbazi.server.domain.policy.api.dto.request.SearchPolicyAutoSaveRequest;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse;
import com.chungbazi.server.domain.policy.api.dto.response.RecentSearchPolicyListResponse;
import com.chungbazi.server.domain.policy.application.PolicySearchService;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.PolicySortType;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.resolver.CurrentUser;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(required = false) PolicyCategoryType category,
            @RequestParam(defaultValue = "LATEST") PolicySortType sort,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return CommonResponse.onSuccess(policySearchService.searchPolicies(user, keyword, category, sort, cursor, size));
    }

    @PatchMapping("/recent-search/auto-save")
    public CommonResponse<String> updateSearchPolicyAutoSaveEnabled(
            @CurrentUser User user,
            @RequestBody SearchPolicyAutoSaveRequest request
    ) {
        policySearchService.updateSearchPolicyAutoSaveEnabled(user, request.enabled());
        return CommonResponse.onSuccess("자동 저장 설정이 완료되었습니다.");
    }

    @GetMapping("/recent-search")
    public CommonResponse<RecentSearchPolicyListResponse> getRecentSearchPolicies(@CurrentUser User user) {
        return CommonResponse.onSuccess(policySearchService.getRecentSearchPolicies(user));
    }

    @DeleteMapping("/recent-search/{keywordId}")
    public CommonResponse<String> deleteRecentSearchPolicy(
            @CurrentUser User user,
            @PathVariable Long keywordId
    ) {
        policySearchService.deleteRecentSearchPolicy(user, keywordId);
        return CommonResponse.onSuccess("최근 검색어 삭제가 완료되었습니다.");
    }

    @DeleteMapping("/recent-search")
    public CommonResponse<String> deleteAllRecentSearchPolicies(@CurrentUser User user) {
        policySearchService.deleteAllRecentSearchPolicies(user);
        return CommonResponse.onSuccess("최근 검색어 전체 삭제가 완료되었습니다.");
    }
}
