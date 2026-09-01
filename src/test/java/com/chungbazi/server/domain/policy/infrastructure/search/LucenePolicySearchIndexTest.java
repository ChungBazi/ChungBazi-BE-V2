package com.chungbazi.server.domain.policy.infrastructure.search;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LucenePolicySearchIndexTest {

    private LucenePolicySearchIndex searchIndex;

    @BeforeEach
    void setUp() throws Exception {
        searchIndex = new LucenePolicySearchIndex();
    }

    @AfterEach
    void tearDown() throws Exception {
        searchIndex.close();
    }

    @Test
    @DisplayName("한글 검색어와 관련된 정책 ID와 BM25 점수를 반환한다")
    void searchesKoreanPolicyDocuments() throws Exception {
        searchIndex.rebuild(List.of(
                policy(1L, "청년 창업 지원", "예비 창업자를 지원합니다"),
                policy(2L, "청년 월세 지원", "주거비를 지원합니다")
        ));

        List<PolicySearchResult> results = searchIndex.search("창업", 10);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().policyId()).isEqualTo(1L);
        assertThat(results.getFirst().score()).isPositive();
    }

    @Test
    @DisplayName("빈 검색어거나 요청 개수가 유효하지 않으면 빈 결과를 반환한다")
    void returnsEmptyResultForInvalidRequest() throws Exception {
        searchIndex.rebuild(List.of(
                policy(1L, "청년 창업 지원", "예비 창업자를 지원합니다")
        ));

        assertThat(searchIndex.search(" ", 10)).isEmpty();
        assertThat(searchIndex.search("창업", 0)).isEmpty();
    }

    private PolicySearchDocument policy(Long policyId, String title, String summary) {
        return PolicySearchDocument.builder()
                .policyId(policyId)
                .title(title)
                .summary(summary)
                .supportContent("")
                .build();
    }
}
