package com.chungbazi.server.domain.policy.domain.repository;

import com.chungbazi.server.domain.policy.domain.entity.RecentViewedPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecentViewedPolicyRepository extends JpaRepository<RecentViewedPolicy, Long> {
}
