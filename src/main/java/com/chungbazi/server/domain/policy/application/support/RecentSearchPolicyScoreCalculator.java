package com.chungbazi.server.domain.policy.application.support;

import com.chungbazi.server.domain.policy.application.dto.RecentKeywordSearchResults;
import com.chungbazi.server.domain.policy.domain.entity.RecentSearchKeyword;
import com.chungbazi.server.domain.policy.domain.repository.RecentSearchKeywordRepository;
import com.chungbazi.server.domain.policy.infrastructure.search.LucenePolicySearchIndex;
import com.chungbazi.server.domain.policy.infrastructure.search.PolicySearchResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecentSearchPolicyScoreCalculator {

    private static final int RECENT_KEYWORD_SIZE = 5;
    private static final int SEARCH_RESULT_SIZE = 20;
    private static final int MAX_RECOMMENDATION_SCORE = 15;
    private static final double SCORE_SCALE = 10.0;
    private static final double RECENCY_DECAY_PER_RANK = 0.1;
    private static final double MIN_RECENCY_WEIGHT = 0.6;

    private final RecentSearchKeywordRepository recentSearchKeywordRepository;
    private final LucenePolicySearchIndex policySearchIndex;

    public Map<Long, Integer> calculateScores(Long userId) {
        List<RecentSearchKeyword> recentKeywords = recentSearchKeywordRepository
                .findRecentSearchKeywords(
                        userId,
                        null,
                        null,
                        PageRequest.of(0, RECENT_KEYWORD_SIZE)
                );

        if (recentKeywords.isEmpty()) {
            return Map.of();
        }

        try {
            return calculateScores(searchAll(recentKeywords));
        } catch (IOException | ParseException exception) {
            log.warn("최근 검색어 정책 점수 계산 실패. userId={}", userId, exception);
            return Map.of();
        }
    }

    private List<RecentKeywordSearchResults> searchAll(List<RecentSearchKeyword> recentKeywords)
            throws IOException, ParseException {
        List<RecentKeywordSearchResults> searchResults = new ArrayList<>();

        for (int rank = 0; rank < recentKeywords.size(); rank++) {
            List<PolicySearchResult> results = policySearchIndex.search(
                    recentKeywords.get(rank).getKeyword(),
                    SEARCH_RESULT_SIZE
            );
            searchResults.add(new RecentKeywordSearchResults(rank, results));
        }
        return searchResults;
    }

    private Map<Long, Integer> calculateScores(List<RecentKeywordSearchResults> keywordResults) {
        double maxBm25Score = keywordResults.stream()
                .flatMap(result -> result.results().stream())
                .mapToDouble(PolicySearchResult::score)
                .max()
                .orElse(0.0);

        if (maxBm25Score <= 0) {
            return Map.of();
        }

        Map<Long, Double> combinedScores = new HashMap<>();
        for (RecentKeywordSearchResults keywordResult : keywordResults) {
            double recencyWeight = Math.max(
                    MIN_RECENCY_WEIGHT,
                    1.0 - keywordResult.rank() * RECENCY_DECAY_PER_RANK
            );

            for (PolicySearchResult result : keywordResult.results()) {
                double normalizedScore = result.score() / maxBm25Score;
                combinedScores.merge(
                        result.policyId(),
                        normalizedScore * recencyWeight,
                        Double::sum
                );
            }
        }

        Map<Long, Integer> recommendationScores = new HashMap<>();
        combinedScores.forEach((policyId, combinedScore) -> recommendationScores.put(
                policyId,
                (int) Math.round(Math.min(
                        MAX_RECOMMENDATION_SCORE,
                        combinedScore * SCORE_SCALE
                ))
        ));
        return Map.copyOf(recommendationScores);
    }
}
