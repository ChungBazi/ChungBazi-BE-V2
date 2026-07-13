package com.chungbazi.server.domain.policy.api;

import com.chungbazi.server.domain.policy.api.docs.RecentSearchDocs;
import com.chungbazi.server.domain.policy.api.dto.request.SearchKeywordAutoSaveRequest;
import com.chungbazi.server.domain.policy.api.dto.response.RecentSearchKeywordListResponse;
import com.chungbazi.server.domain.policy.application.PolicySearchService;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.resolver.CurrentUser;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/recent-searches")
public class RecentSearchController implements RecentSearchDocs {

    private final PolicySearchService policySearchService;

    @Override
    @GetMapping("")
    public CommonResponse<RecentSearchKeywordListResponse> getRecentSearchKeywords(
            @CurrentUser User user,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return CommonResponse.onSuccess(policySearchService.getRecentSearchKeywords(user, cursor, size));
    }

    @Override
    @DeleteMapping("")
    public CommonResponse<String> deleteAllRecentSearchKeywords(@CurrentUser User user) {
        policySearchService.deleteAllRecentSearchKeywords(user);
        return CommonResponse.onSuccess("최근 검색어 전체 삭제가 완료되었습니다.");
    }

    @Override
    @DeleteMapping("/{keywordId}")
    public CommonResponse<String> deleteRecentSearchKeyword(
            @CurrentUser User user,
            @PathVariable Long keywordId
    ) {
        policySearchService.deleteRecentSearchKeyword(user,keywordId);
        return CommonResponse.onSuccess("최근 검색어 삭제가 완료되었습니다.");
    }

    @Override
    @PatchMapping("/auto-save")
    public CommonResponse<String> updateSearchKeywordAutoSaveEnabled(
            @CurrentUser User user,
            @RequestBody SearchKeywordAutoSaveRequest request
    ) {
        policySearchService.updateSearchKeywordAutoSaveEnabled(user, request.enabled());
        return CommonResponse.onSuccess("자동 저장 설정이 완료되었습니다.");
    }
}
