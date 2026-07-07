package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse;
import com.chungbazi.server.domain.policy.api.dto.response.RecentSearchPolicyListResponse;
import com.chungbazi.server.domain.policy.application.cursor.PolicyCursor;
import com.chungbazi.server.domain.policy.application.cursor.PolicyCursorParser;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.RecentSearchPolicy;
import com.chungbazi.server.domain.policy.domain.repository.RecentSearchPolicyRepository;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.PolicySortType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.policy.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.exception.PolicyException;
import com.chungbazi.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicySearchService {

    private final PolicyRepository policyRepository;
    private final RecentSearchPolicyRepository recentSearchPolicyRepository;
    private final PolicyListResponseAssembler policyListResponseAssembler;

    public PolicyListResponse searchPolicies(User user, String keyword, String cursor, int size) {
        PolicySortType sort = PolicySortType.LATEST;
        String normalizedKeyword = normalizeKeyword(keyword);
        PolicyCursor decodedCursor = PolicyCursorParser.decode(cursor, sort);

        List<Policy> fetchedPolicies = policyRepository.searchPolicies(
                normalizedKeyword,
                RecruitmentStatus.CLOSED,
                user.getSidoCode(),
                user.getSigunguCode(),
                decodedCursor == null ? null : decodedCursor.registeredAt(),
                decodedCursor == null ? null : decodedCursor.policyId(),
                PageRequest.of(0, size + 1)
        );

        long totalCount = policyRepository.countSearchPolicies(
                normalizedKeyword,
                RecruitmentStatus.CLOSED,
                user.getSidoCode(),
                user.getSigunguCode()
        );
        return policyListResponseAssembler.assemble(user, sort, fetchedPolicies, totalCount, size);
    }

    public RecentSearchPolicyListResponse getRecentSearchPolicies(User user) {
        List<RecentSearchPolicy> recentSearchPolicies =
                recentSearchPolicyRepository.findTop5ByUserIdOrderByLastSearchedAtDesc(user.getId());

        return RecentSearchPolicyListResponse.from(recentSearchPolicies);
    }

    @Transactional
    public void deleteRecentSearchPolicy(User user, Long keywordId) {
        RecentSearchPolicy recentSearchPolicy = recentSearchPolicyRepository
                .findByUserIdAndId(user.getId(), keywordId)
                .orElseThrow(() -> new PolicyException(PolicyErrorCode.RECENT_SEARCH_KEYWORD_NOT_FOUND));

        recentSearchPolicyRepository.delete(recentSearchPolicy);
    }

    @Transactional
    public void deleteAllRecentSearchPolicies(User user) {
        recentSearchPolicyRepository.deleteAllByUserId(user.getId());
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new PolicyException(PolicyErrorCode.INVALID_SEARCH_KEYWORD);
        }
        return keyword.trim();
    }
}
