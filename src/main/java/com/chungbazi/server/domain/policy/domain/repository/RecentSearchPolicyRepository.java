package com.chungbazi.server.domain.policy.domain.repository;

import com.chungbazi.server.domain.policy.domain.entity.RecentSearchPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecentSearchPolicyRepository extends JpaRepository<RecentSearchPolicy, Long> {
    List<RecentSearchPolicy> findTop5ByUserIdOrderByLastSearchedAtDesc(Long userId);
    Optional<RecentSearchPolicy> findByUserIdAndKeyword(Long userId, String keyword);
    Optional<RecentSearchPolicy> findByUserIdAndId(Long userId, Long id);
    void deleteAllByUserId(Long userId);
}
