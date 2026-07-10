package com.chungbazi.server.domain.policy.api;

import com.chungbazi.server.domain.policy.api.dto.request.SearchKeywordAutoSaveRequest;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse;
import com.chungbazi.server.domain.policy.api.dto.response.RecentSearchKeywordListResponse;
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
    @GetMapping("/search/suggestions")
    public CommonResponse<SearchSuggestionResponse> getSearchSuggestions(
            @CurrentUser User user,
            @RequestParam String keyword
    ) {
        return CommonResponse.onSuccess(policySearchService.getSearchSuggestions(user, keyword));
    }

    @Override
    @PatchMapping("/recent-search/auto-save")
    public CommonResponse<String> updateSearchKeywordAutoSaveEnabled(
            @CurrentUser User user,
            @RequestBody SearchKeywordAutoSaveRequest request
    ) {
        policySearchService.updateSearchKeywordAutoSaveEnabled(user, request.enabled());
        return CommonResponse.onSuccess("자동 저장 설정이 완료되었습니다.");
    }

    @Override
    @GetMapping("/recent-search")
    public CommonResponse<RecentSearchKeywordListResponse> getRecentSearchKeywords(
            @CurrentUser User user,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return CommonResponse.onSuccess(policySearchService.getRecentSearchKeywords(user, cursor, size));
    }

    @Override
    @DeleteMapping("/recent-search/{keywordId}")
    public CommonResponse<String> deleteRecentSearchKeyword(
            @CurrentUser User user,
            @PathVariable Long keywordId
    ) {
        policySearchService.deleteRecentSearchKeyword(user, keywordId);
        return CommonResponse.onSuccess("최근 검색어 삭제가 완료되었습니다.");
    }

    @Override
    @DeleteMapping("/recent-search")
    public CommonResponse<String> deleteAllRecentSearchKeywords(@CurrentUser User user) {
        policySearchService.deleteAllRecentSearchKeywords(user);
        return CommonResponse.onSuccess("최근 검색어 전체 삭제가 완료되었습니다.");
    }
}
