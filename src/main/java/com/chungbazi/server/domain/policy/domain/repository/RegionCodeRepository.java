package com.chungbazi.server.domain.policy.domain.repository;

import com.chungbazi.server.domain.policy.domain.entity.RegionCode;
import com.chungbazi.server.domain.policy.domain.type.SidoCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegionCodeRepository extends JpaRepository<RegionCode, String> {
    List<RegionCode> findAllBySidoCode(SidoCode sidoCode);
}
