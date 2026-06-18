package com.chungbazi.server.domain.policy.repository;

import com.chungbazi.server.domain.policy.entity.PolicyDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyDetailRepository extends JpaRepository<PolicyDetail, Long> {
}
