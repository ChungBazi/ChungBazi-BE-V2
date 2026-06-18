package com.chungbazi.server.domain.policy.repository;

import com.chungbazi.server.domain.policy.entity.PolicyCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyCategoryRepository extends JpaRepository<PolicyCategory, Long> {
}
