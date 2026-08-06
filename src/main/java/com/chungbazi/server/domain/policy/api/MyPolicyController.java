package com.chungbazi.server.domain.policy.api;

import com.chungbazi.server.domain.policy.api.docs.MyPolicyDocs;
import com.chungbazi.server.domain.policy.api.dto.response.MyPolicyDeadlineResponse;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse;
import com.chungbazi.server.domain.policy.application.MyPolicyService;
import com.chungbazi.server.domain.policy.domain.type.PolicySortType;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.resolver.CurrentUser;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/my-policy")
public class MyPolicyController implements MyPolicyDocs {

    private final MyPolicyService myPolicyService;

    @Override
    @GetMapping("/policies/deadline")
    public CommonResponse<MyPolicyDeadlineResponse> getMyPolicyDeadline(@CurrentUser User user) {

        return CommonResponse.onSuccess(myPolicyService.getMyPolicyDeadline(user));
    }

    @Override
    @GetMapping("/policies/deadline/date")
    public CommonResponse<PolicyListResponse> getDeadlinePoliciesByDate(
            @CurrentUser User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate,
            @RequestParam(defaultValue = "LATEST") PolicySortType sort,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {

        return CommonResponse.onSuccess(myPolicyService.getDeadlinePoliciesByDate(user, targetDate, sort, cursor, size));
    }
}
