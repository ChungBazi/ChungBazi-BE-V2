package com.chungbazi.server.domain.policy.domain.repository;

import com.chungbazi.server.domain.policy.domain.entity.RecentSearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecentSearchKeywordRepository extends JpaRepository<RecentSearchKeyword, Long> {
    List<RecentSearchKeyword> findTop10ByUserIdOrderByLastSearchedAtDesc(Long userId);
    Optional<RecentSearchKeyword> findByUserIdAndKeyword(Long userId, String keyword);
    Optional<RecentSearchKeyword> findByUserIdAndId(Long userId, Long id);
    void deleteAllByUserId(Long userId);
}
