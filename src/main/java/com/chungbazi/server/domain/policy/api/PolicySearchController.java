package com.chungbazi.server.domain.policy.api;

import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse;
import com.chungbazi.server.domain.policy.api.dto.response.SearchSuggestionResponse;
import com.chungbazi.server.domain.policy.api.docs.PolicySearchDocs;
import com.chungbazi.server.domain.policy.application.PolicySearchService;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.PolicySortType;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.resolver.CurrentUser;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/policies")
public class PolicySearchController implements PolicySearchDocs {

    private final PolicySearchService policySearchService;

    @Override
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

    @Override
    @GetMapping("/search-suggestions")
    public CommonResponse<SearchSuggestionResponse> getSearchSuggestions(
            @CurrentUser User user,
            @RequestParam String keyword
    ) {
        return CommonResponse.onSuccess(policySearchService.getSearchSuggestions(user, keyword));
    }
}
