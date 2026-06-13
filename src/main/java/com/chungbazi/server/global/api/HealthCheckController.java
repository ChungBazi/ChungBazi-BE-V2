package com.chungbazi.server.global.api;

import com.chungbazi.server.global.api.docs.HealthCheckDocs;
import com.chungbazi.server.global.common.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/global")
public class HealthCheckController implements HealthCheckDocs {

    @Override
    @GetMapping("/health-check")
    public CommonResponse<String> healthCheck() {
        return CommonResponse.onSuccess("Server is running!");
    }
}
