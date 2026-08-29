package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.api.dto.response.SidoResponse;
import com.chungbazi.server.domain.policy.api.dto.response.SigunguResponse;
import com.chungbazi.server.domain.policy.domain.entity.RegionCode;
import com.chungbazi.server.domain.policy.domain.repository.RegionCodeRepository;
import com.chungbazi.server.domain.policy.domain.type.SidoCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionService {

    private final RegionCodeRepository regionCodeRepository;

    public List<SidoResponse> getSidos() {
        return Arrays.stream(SidoCode.values())
                .map(SidoResponse::from)
                .toList();
    }

    public List<SigunguResponse> getSigungus(SidoCode sidoCode) {
        return regionCodeRepository.findAllBySidoCode(sidoCode).stream()
                .sorted(Comparator.comparing(RegionCode::getSigunguCode))
                .map(SigunguResponse::from)
                .toList();
    }
}
