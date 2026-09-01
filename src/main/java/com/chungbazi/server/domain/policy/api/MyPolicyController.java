package com.chungbazi.server.domain.policy.api;

import com.chungbazi.server.domain.policy.api.docs.MyPolicyDocs;
import com.chungbazi.server.domain.policy.api.dto.request.PolicyMemoRequest;
import com.chungbazi.server.domain.policy.api.dto.response.CalendarResponse;
import com.chungbazi.server.domain.policy.api.dto.response.MyPolicyDeadlineResponse;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyMemoResponse;
import com.chungbazi.server.domain.policy.application.MyPolicyService;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.PolicyListSortType;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.resolver.CurrentUser;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/my-policy")
public class MyPolicyController implements MyPolicyDocs {

    private final MyPolicyService myPolicyService;

    @Override
    @GetMapping("/deadline")
    public CommonResponse<MyPolicyDeadlineResponse> getMyPolicyDeadline(@CurrentUser User user) {

        return CommonResponse.onSuccess(myPolicyService.getMyPolicyDeadline(user));
    }

    @Override
    @GetMapping("/deadline/date")
    public CommonResponse<PolicyListResponse> getDeadlinePoliciesByDate(
            @CurrentUser User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate
    ) {

        return CommonResponse.onSuccess(myPolicyService.getDeadlinePoliciesByDate(user, targetDate));
    }

    @Override
    @GetMapping("/deadline/upcoming")
    public CommonResponse<PolicyListResponse> getUpcomingDeadlinePoliciesWithinTwoWeeks(
            @CurrentUser User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate
    ) {

        return CommonResponse.onSuccess(myPolicyService.getUpcomingDeadlinePoliciesWithinTwoWeeks(user, targetDate));
    }

    @Override
    @GetMapping("/open-ended")
    public CommonResponse<PolicyListResponse> getOpenEndedLikedPolicies(
            @CurrentUser User user,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {

        return CommonResponse.onSuccess(myPolicyService.getOpenEndedLikedPolicies(user, cursor, size));
    }

    @Override
    @GetMapping
    public CommonResponse<PolicyListResponse> getMyPoliciesByCategory(
            @CurrentUser User user,
            @RequestParam(required = false) PolicyCategoryType category,
            @RequestParam(defaultValue = "LATEST") PolicyListSortType sort,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {

        return CommonResponse.onSuccess(myPolicyService.getMyPoliciesByCategory(user, category, sort, cursor, size));
    }

    @Override
    @GetMapping("/calendar")
    public CommonResponse<CalendarResponse> getMyCalendar(
            @CurrentUser User user,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth targetMonth
    ) {

        return CommonResponse.onSuccess(myPolicyService.getMyCalendar(user, targetMonth));
    }

    @Override
    @GetMapping("/{policyId}/memo")
    public CommonResponse<PolicyMemoResponse> getPolicyMemo(
            @CurrentUser User user,
            @PathVariable Long policyId
    ) {

        return CommonResponse.onSuccess(myPolicyService.getPolicyMemo(user, policyId));
    }

    @Override
    @PutMapping("/{policyId}/memo")
    public CommonResponse<String> updatePolicyMemo(
            @CurrentUser User user,
            @PathVariable Long policyId,
            @Valid @RequestBody PolicyMemoRequest request
    ) {

        myPolicyService.updatePolicyMemo(user, policyId, request.memo());
        return CommonResponse.onSuccess("정책 메모 작성 및 수정이 완료되었습니다.");
    }
}
