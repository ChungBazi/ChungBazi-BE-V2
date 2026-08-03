package com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chungbazi.server.domain.policy.domain.entity.RegionCode;
import com.chungbazi.server.domain.policy.domain.type.SidoCode;
import com.chungbazi.server.domain.policy.domain.type.internal.PolicyRegionMapping;
import com.chungbazi.server.domain.policy.infrastructure.persistence.RegionCodeProvider;
import java.util.List;
import org.junit.jupiter.api.Test;

class YouthPolicyRegionMapperTest {

    private final RegionCodeProvider regionCodeProvider = mock(RegionCodeProvider.class);
    private final YouthPolicyRegionMapper mapper = new YouthPolicyRegionMapper(regionCodeProvider);

    @Test
    void normalizesLegacyGwangjuAndJeonnamRegionCodesToJeonnamGwangjuCodes() {
        when(regionCodeProvider.getRegionCodes()).thenReturn(List.of(
                regionCode("12110", "목포시"),
                regionCode("12210", "동구"),
                regionCode("12870", "신안군")
        ));

        PolicyRegionMapping mapping = mapper.toRegionMapping("46110,29110");

        assertThat(mapping.national()).isFalse();
        assertThat(mapping.scopes())
                .extracting(scope -> scope.regionCode().getSigunguCode())
                .containsExactly("12110", "12210");
    }

    @Test
    void removesDuplicatesAfterNormalizingLegacyRegionCodes() {
        when(regionCodeProvider.getRegionCodes()).thenReturn(List.of(
                regionCode("12110", "목포시"),
                regionCode("12210", "동구")
        ));

        PolicyRegionMapping mapping = mapper.toRegionMapping("46110,12110");

        assertThat(mapping.national()).isFalse();
        assertThat(mapping.scopes())
                .extracting(scope -> scope.regionCode().getSigunguCode())
                .containsExactly("12110");
    }

    private RegionCode regionCode(String sigunguCode, String sigunguName) {
        return RegionCode.createRegionCode(sigunguCode, sigunguName, SidoCode.JEONNAM_GWANGJU);
    }
}
