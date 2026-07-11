package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse;
import com.chungbazi.server.domain.policy.api.dto.response.RecentSearchKeywordListResponse;
import com.chungbazi.server.domain.policy.api.dto.response.SearchSuggestionResponse;
import com.chungbazi.server.domain.policy.application.cursor.PolicyCursor;
import com.chungbazi.server.domain.policy.application.cursor.PolicyCursorParser;
import com.chungbazi.server.domain.policy.application.cursor.RecentSearchKeywordCursor;
import com.chungbazi.server.domain.policy.application.cursor.RecentSearchKeywordCursorParser;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.RecentSearchKeyword;
import com.chungbazi.server.domain.policy.domain.repository.RecentSearchKeywordRepository;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.PolicySortType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.policy.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.exception.PolicyException;
import com.chungbazi.server.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PolicySearchService {

    private static final int SEARCH_SUGGESTION_LIMIT = 10;

    private final PolicyRepository policyRepository;
    private final RecentSearchKeywordRepository recentSearchKeywordRepository;
    private final PolicyListResponseAssembler policyListResponseAssembler;

    @Transactional
    public PolicyListResponse searchPolicies(
            User user,
            String keyword,
            PolicyCategoryType category,
            PolicySortType sort,
            String cursor,
            int size
    ) {
        String normalizedKeyword = normalizeKeyword(keyword);
        PolicyCursor decodedCursor = PolicyCursorParser.decode(cursor, sort);
        // 최근 검색어 저장
        if (user.isSearchKeywordAutoSaveEnabled()) {
            saveRecentSearchKeyword(user, normalizedKeyword);
        }

        List<Policy> fetchedPolicies = policyRepository.searchPolicies(
                normalizedKeyword,
                category,
                sort,
                RecruitmentStatus.CLOSED,
                user.getSidoCode(),
                user.getSigunguCode(),
                decodedCursor == null ? null : decodedCursor.registeredAt(),
                decodedCursor == null ? null : decodedCursor.applyEndDate(),
                decodedCursor == null ? null : decodedCursor.policyId(),
                PageRequest.of(0, size + 1)
        );

        long totalCount = policyRepository.countSearchPolicies(
                normalizedKeyword,
                category,
                RecruitmentStatus.CLOSED,
                user.getSidoCode(),
                user.getSigunguCode()
        );
        return policyListResponseAssembler.assemble(user, sort, fetchedPolicies, totalCount, size);
    }

    public SearchSuggestionResponse getSearchSuggestions(User user, String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);

        List<String> suggestions = policyRepository.findSearchSuggestions(
                normalizedKeyword,
                RecruitmentStatus.CLOSED,
                user.getSidoCode(),
                user.getSigunguCode(),
                SEARCH_SUGGESTION_LIMIT
        );

        return SearchSuggestionResponse.of(suggestions);
    }

    public RecentSearchKeywordListResponse getRecentSearchKeywords(User user, String cursor, int size) {
        RecentSearchKeywordCursor decodedCursor = RecentSearchKeywordCursorParser.decode(cursor);

        List<RecentSearchKeyword> recentSearchKeywords =
                recentSearchKeywordRepository.findRecentSearchKeywords(
                        user.getId(),
                        decodedCursor == null ? null : decodedCursor.lastSearchedAt(),
                        decodedCursor == null ? null : decodedCursor.keywordId(),
                        PageRequest.of(0, size + 1)
                );

        return RecentSearchKeywordListResponse.of(user, recentSearchKeywords, size);
    }

    @Transactional
    public void updateSearchKeywordAutoSaveEnabled(User user, boolean enabled) {
        user.updateSearchKeywordAutoSaveEnabled(enabled);
    }

    @Transactional
    public void saveRecentSearchKeyword(User user, String keyword) {
        recentSearchKeywordRepository.findByUserIdAndKeyword(user.getId(), keyword)
                .ifPresentOrElse(
                        RecentSearchKeyword::refresh,
                        () -> saveNewRecentSearchKeyword(user.getId(), keyword)
                );
    }

    @Transactional
    public void deleteRecentSearchKeyword(User user, Long keywordId) {
        RecentSearchKeyword recentSearchKeyword = recentSearchKeywordRepository.findByUserIdAndId(user.getId(), keywordId)
                .orElseThrow(() -> new PolicyException(PolicyErrorCode.RECENT_SEARCH_KEYWORD_NOT_FOUND));

        recentSearchKeywordRepository.delete(recentSearchKeyword);
    }

    @Transactional
    public void deleteAllRecentSearchKeywords(User user) {
        recentSearchKeywordRepository.deleteAllByUserId(user.getId());
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new PolicyException(PolicyErrorCode.INVALID_SEARCH_KEYWORD);
        }
        return keyword.trim();
    }

    private void saveNewRecentSearchKeyword(Long userId, String keyword) {
        try {
            recentSearchKeywordRepository.save(
                    RecentSearchKeyword.create(userId, keyword)
            );
        } catch (DataIntegrityViolationException exception) {
            RecentSearchKeyword recentSearchKeyword =
                    recentSearchKeywordRepository.findByUserIdAndKeyword(userId, keyword)
                            .orElseThrow(() -> exception);

            recentSearchKeyword.refresh();
        }
    }
}
