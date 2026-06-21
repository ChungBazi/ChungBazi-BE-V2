package com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.mapper;

import com.chungbazi.server.domain.policy.domain.vo.PolicyRegionMapping;
import com.chungbazi.server.domain.policy.domain.vo.RegionScope;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.PolicyRegion;
import com.chungbazi.server.domain.policy.domain.entity.RegionCode;
import com.chungbazi.server.domain.policy.domain.vo.SidoCode;
import com.chungbazi.server.domain.policy.domain.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.domain.exception.PolicyException;
import com.chungbazi.server.domain.policy.infrastructure.persistence.RegionCodeProvider;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class YouthPolicyRegionMapper {

    private static final int SIGUNGU_CODE_LENGTH = 5;

    private final RegionCodeProvider regionCodeProvider;

    public PolicyRegionMapping toRegionMapping(String policyZipCode) {
        List<RegionCode> regionMaster = regionCodeProvider.getRegionCodes();
        if (regionMaster.isEmpty()) {
            throw new PolicyException(PolicyErrorCode.REGION_NOT_INITIALIZED);
        }

        Set<String> requestedCodes = parseSigunguCodes(policyZipCode);
        Map<String, RegionCode> regionByCode = regionMaster.stream()
                .collect(Collectors.toMap(RegionCode::getSigunguCode, Function.identity()));

        if (!regionByCode.keySet().containsAll(requestedCodes)) {
            throw new PolicyException(PolicyErrorCode.INVALID_POLICY_REGION);
        }

        List<RegionCode> requestedRegions = requestedCodes.stream()
                .map(regionByCode::get)
                .toList();

        //전국 정책일 경우
        if (containsAllRegions(requestedRegions, regionMaster)) {
            return new PolicyRegionMapping(true, List.of());
        }

        //특정 지역 정책일 경우
        return new PolicyRegionMapping(false, compressRegionScopes(requestedRegions, regionMaster));
    }

    public List<PolicyRegion> toPolicyRegions(Policy policy, PolicyRegionMapping mapping) {
        if (mapping.national()) {
            return List.of();
        }

        return mapping.scopes().stream()
                .map(scope -> scope.regionCode() == null
                        ? PolicyRegion.createSidoPolicyRegion(policy, scope.sidoCode())
                        : PolicyRegion.createSigunguPolicyRegion(policy, scope.regionCode()))
                .toList();
    }

    private Set<String> parseSigunguCodes(String value) {
        String normalized = YouthPolicyTextUtils.trimToNull(value);
        if (normalized == null) {
            throw new PolicyException(PolicyErrorCode.INVALID_POLICY_REGION);
        }

        Set<String> codes = Arrays.stream(normalized.split(","))
                .map(YouthPolicyTextUtils::trimToNull)
                .collect(Collectors.toSet());

        if (codes.isEmpty() || codes.contains(null) || codes.stream().anyMatch(code -> !isSigunguCode(code))) {
            throw new PolicyException(PolicyErrorCode.INVALID_POLICY_REGION);
        }
        return codes;
    }

    private boolean isSigunguCode(String value) {
        return value != null
                && value.length() == SIGUNGU_CODE_LENGTH
                && value.chars().allMatch(Character::isDigit);
    }

    private boolean containsAllRegions(List<RegionCode> requestedRegions, List<RegionCode> regionMaster) {
        return requestedRegions.size() == regionMaster.size();
    }

    private List<RegionScope> compressRegionScopes(
            List<RegionCode> requestedRegions,
            List<RegionCode> regionMaster
    ) {
        Map<SidoCode, List<RegionCode>> requestedBySido = groupBySido(requestedRegions);
        Map<SidoCode, List<RegionCode>> masterBySido = groupBySido(regionMaster);

        return requestedBySido.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(SidoCode::getCode)))
                .flatMap(entry -> {
                    SidoCode sidoCode = entry.getKey();
                    List<RegionCode> requested = entry.getValue();
                    List<RegionCode> master = masterBySido.getOrDefault(sidoCode, List.of());

                    if (!master.isEmpty() && requested.size() == master.size()) {
                        return List.of(RegionScope.sido(sidoCode)).stream();
                    }
                    return requested.stream()
                            .sorted(Comparator.comparing(RegionCode::getSigunguCode))
                            .map(RegionScope::sigungu);
                })
                .toList();
    }

    private Map<SidoCode, List<RegionCode>> groupBySido(List<RegionCode> regions) {
        return regions.stream()
                .collect(Collectors.groupingBy(
                        RegionCode::getSidoCode,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }
}
