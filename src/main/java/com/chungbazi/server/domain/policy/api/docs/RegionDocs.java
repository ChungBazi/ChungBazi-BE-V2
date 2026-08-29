package com.chungbazi.server.domain.policy.api.docs;

import com.chungbazi.server.domain.policy.api.dto.response.SidoResponse;
import com.chungbazi.server.domain.policy.api.dto.response.SigunguResponse;
import com.chungbazi.server.domain.policy.domain.type.SidoCode;
import com.chungbazi.server.global.common.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "[Region]", description = "지역 코드 관련 API")
public interface RegionDocs {
    @Operation(
            summary = "시도 목록 조회 API",
            description = """
                    ### ResponseBody
                    ---
                    - `sidoCode`: 시도 코드. 온보딩/정책 추천 기준 저장 시 사용하는 값
                    - `sidoName`: 화면에 표시할 시도명
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "시도 목록이 성공적으로 조회됐습니다."
            )
    })
    CommonResponse<List<SidoResponse>> getSidos();

    @Operation(
            summary = "시군구 목록 조회 API",
            description = """
                    ### Query Parameter
                    ---
                    - `sidoCode`: 조회할 시도 코드
                    
                    ### ResponseBody
                    ---
                    - `sigunguCode`: 시군구 코드. 온보딩/정책 추천 기준 저장 시 사용하는 값
                    - `sigunguName`: 화면에 표시할 시군구명
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "시군구 목록이 성공적으로 조회됐습니다."
            )
    })
    CommonResponse<List<SigunguResponse>> getSigungus(
            @Parameter(description = "조회할 시도 코드", example = "SEOUL")
            @RequestParam SidoCode sidoCode
    );
}
