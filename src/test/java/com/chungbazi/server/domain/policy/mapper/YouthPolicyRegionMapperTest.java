package com.chungbazi.server.domain.policy.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.chungbazi.server.domain.policy.dto.internal.PolicyRegionMapping;
import com.chungbazi.server.domain.policy.entity.RegionCode;
import com.chungbazi.server.domain.policy.enums.SidoCode;
import com.chungbazi.server.domain.policy.service.RegionCodeProvider;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class YouthPolicyRegionMapperTest {

    @Mock
    private RegionCodeProvider regionCodeProvider;

    private YouthPolicyRegionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new YouthPolicyRegionMapper(regionCodeProvider);
        when(regionCodeProvider.getRegionCodes()).thenReturn(List.of(
                region("11110", "종로구", SidoCode.SEOUL),
                region("11680", "강남구", SidoCode.SEOUL),
                region("26110", "중구", SidoCode.BUSAN)
        ));
    }

    @Test
    void compressesAllRegionsIntoNationalPolicy() {
        PolicyRegionMapping result = mapper.toRegionMapping("11110,11680,26110");

        assertThat(result.national()).isTrue();
        assertThat(result.scopes()).isEmpty();
    }

    @Test
    void compressesAllSigunguOfOneSidoIntoSidoScope() {
        PolicyRegionMapping result = mapper.toRegionMapping("11110,11680");

        assertThat(result.national()).isFalse();
        assertThat(result.scopes()).hasSize(1);
        assertThat(result.scopes().getFirst().sidoCode()).isEqualTo(SidoCode.SEOUL);
        assertThat(result.scopes().getFirst().regionCode()).isNull();
    }

    @Test
    void keepsPartialRegionAsSigunguScope() {
        PolicyRegionMapping result = mapper.toRegionMapping("11680");

        assertThat(result.national()).isFalse();
        assertThat(result.scopes()).hasSize(1);
        assertThat(result.scopes().getFirst().regionCode().getSigunguCode()).isEqualTo("11680");
    }

    private RegionCode region(String code, String name, SidoCode sidoCode) {
        return RegionCode.createRegionCode(code, name, sidoCode);
    }
}
