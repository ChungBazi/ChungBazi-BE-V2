package com.chungbazi.server.domain.policy.api;

import com.chungbazi.server.domain.policy.api.docs.MyPolicyDocs;
import com.chungbazi.server.domain.policy.api.dto.response.MyPolicyDeadlineResponse;
import com.chungbazi.server.domain.policy.application.MyPolicyService;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.global.common.CommonResponse;
import com.chungbazi.server.global.resolver.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
