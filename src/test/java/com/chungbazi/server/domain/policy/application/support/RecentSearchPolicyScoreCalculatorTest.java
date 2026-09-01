package com.chungbazi.server.domain.policy.application.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.chungbazi.server.domain.policy.domain.entity.RecentSearchKeyword;
import com.chungbazi.server.domain.policy.domain.repository.RecentSearchKeywordRepository;
import com.chungbazi.server.domain.policy.infrastructure.search.LucenePolicySearchIndex;
import com.chungbazi.server.domain.policy.infrastructure.search.PolicySearchResult;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class RecentSearchPolicyScoreCalculatorTest {

    @Mock
    private RecentSearchKeywordRepository recentSearchKeywordRepository;

    @Mock
    private LucenePolicySearchIndex policySearchIndex;

    private RecentSearchPolicyScoreCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new RecentSearchPolicyScoreCalculator(
                recentSearchKeywordRepository,
                policySearchIndex
        );
    }

    @Test
    @DisplayName("전체 최대 BM25 점수와 검색어 최신성을 기준으로 정책 점수를 계산한다")
    void calculatesNormalizedRecentSearchScores() throws Exception {
        RecentSearchKeyword latestKeyword = keyword("청약");
        RecentSearchKeyword olderKeyword = keyword("주택청약");
        when(recentSearchKeywordRepository.findRecentSearchKeywords(
                eq(1L),
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(List.of(latestKeyword, olderKeyword));
        when(policySearchIndex.search("청약", 20)).thenReturn(List.of(
                new PolicySearchResult(47L, 10.0f),
                new PolicySearchResult(416L, 5.0f)
        ));
        when(policySearchIndex.search("주택청약", 20)).thenReturn(List.of(
                new PolicySearchResult(47L, 10.0f)
        ));

        Map<Long, Integer> scores = calculator.calculateScores(1L);

        assertThat(scores.get(47L)).isEqualTo(15);
        assertThat(scores.get(416L)).isEqualTo(5);
    }

    private RecentSearchKeyword keyword(String value) {
        RecentSearchKeyword keyword = mock(RecentSearchKeyword.class);
        when(keyword.getKeyword()).thenReturn(value);
        return keyword;
    }
}
