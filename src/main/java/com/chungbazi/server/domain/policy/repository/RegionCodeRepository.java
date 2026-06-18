package com.chungbazi.server.domain.policy.repository;

import com.chungbazi.server.domain.policy.entity.RegionCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionCodeRepository extends JpaRepository<RegionCode, String> {
}
