package com.chungbazi.server.domain.policy.infrastructure.persistence;

import com.chungbazi.server.domain.policy.domain.entity.RegionCode;
import com.chungbazi.server.domain.policy.domain.repository.RegionCodeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegionCodeProvider {

    private final RegionCodeRepository regionCodeRepository;

    private volatile List<RegionCode> cachedRegionCodes;

    public List<RegionCode> getRegionCodes() {
        List<RegionCode> regionCodes = cachedRegionCodes;
        if (regionCodes == null) {
            synchronized (this) {
                regionCodes = cachedRegionCodes;
                if (regionCodes == null) {
                    regionCodes = List.copyOf(regionCodeRepository.findAll());
                    cachedRegionCodes = regionCodes;
                }
            }
        }
        return regionCodes;
    }
}
