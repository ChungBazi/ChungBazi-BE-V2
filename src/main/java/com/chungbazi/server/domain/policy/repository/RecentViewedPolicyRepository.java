package com.chungbazi.server.domain.policy.repository;

import com.chungbazi.server.domain.policy.entity.RecentViewedPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecentViewedPolicyRepository extends JpaRepository<RecentViewedPolicy, Long> {
}
