package com.chungbazi.server.domain.policy.application.dto;

import com.chungbazi.server.domain.policy.infrastructure.search.PolicySearchResult;
import java.util.List;

public record RecentKeywordSearchResults(
        int rank,
        List<PolicySearchResult> results
) {
}
