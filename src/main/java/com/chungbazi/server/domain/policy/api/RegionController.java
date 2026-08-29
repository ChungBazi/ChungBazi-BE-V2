package com.chungbazi.server.domain.policy.api;

import com.chungbazi.server.domain.policy.api.docs.RegionDocs;
import com.chungbazi.server.domain.policy.api.dto.response.SidoResponse;
import com.chungbazi.server.domain.policy.api.dto.response.SigunguResponse;
import com.chungbazi.server.domain.policy.application.RegionService;
import com.chungbazi.server.domain.policy.domain.type.SidoCode;
import com.chungbazi.server.global.common.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/regions")
public class RegionController implements RegionDocs {

    private final RegionService regionService;

    @Override
    @GetMapping("/sido")
    public CommonResponse<List<SidoResponse>> getSidos() {
        return CommonResponse.onSuccess(regionService.getSidos());
    }

    @Override
    @GetMapping("/sigungu")
    public CommonResponse<List<SigunguResponse>> getSigungus(@RequestParam SidoCode sidoCode) {
        return CommonResponse.onSuccess(regionService.getSigungus(sidoCode));
    }
}
