package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.api.dto.response.HomePolicyResponse;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse;
import com.chungbazi.server.domain.policy.application.cursor.PolicyCursor;
import com.chungbazi.server.domain.policy.application.cursor.PolicyCursorParser;
import com.chungbazi.server.domain.policy.application.cursor.RecentViewedPolicyCursor;
import com.chungbazi.server.domain.policy.application.cursor.RecentViewedPolicyCursorParser;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.RecentViewedPolicy;
import com.chungbazi.server.domain.policy.domain.repository.RecentViewedPolicyRepository;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.PolicySortType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.policy.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.exception.PolicyException;
import com.chungbazi.server.domain.user.domain.User;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomePolicyService {

    private static final int HOME_SECTION_SIZE = 5;
    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final PolicyRepository policyRepository;
    private final RecentViewedPolicyRepository recentViewedPolicyRepository;
    private final PolicyListResponseAssembler policyListResponseAssembler;
    private final PersonalizedPolicyService personalizedPolicyService;

    public HomePolicyResponse getHomePolicies(User user) {
        LocalDate today = LocalDate.now(SERVICE_ZONE_ID);
        PageRequest sectionPageRequest = PageRequest.of(0, HOME_SECTION_SIZE);

        List<Policy> personalizedPolicies =
                personalizedPolicyService.getPersonalizedPolicyEntities(user, HOME_SECTION_SIZE);

        List<Policy> recentViewedPolicies = fetchRecentViewedPolicies(user, null, sectionPageRequest)
                .stream()
                .map(RecentViewedPolicy::getPolicy)
                .toList();
        List<Policy> popularPolicies = fetchPopularPolicies(
                user,
                null,
                null,
                sectionPageRequest
        );
        List<Policy> upcomingDeadlinePolicies = fetchUpcomingDeadlinePolicies(
                user,
                null,
                null,
                today,
                sectionPageRequest
        );
        List<Policy> latestPolicies = fetchLatestPolicies(
                user,
                null,
                null,
                sectionPageRequest
        );

        List<Policy> homePolicies = Stream.of(
                        personalizedPolicies,
                        recentViewedPolicies,
                        popularPolicies,
                        upcomingDeadlinePolicies,
                        latestPolicies
                )
                .flatMap(List::stream)
                .toList();
        Set<Long> likedPolicyIds = policyListResponseAssembler.findLikedPolicyIds(user.getId(), homePolicies);

        return HomePolicyResponse.builder()
                .personalizedPolicies(policyListResponseAssembler.summarize(personalizedPolicies, likedPolicyIds))
                .recentViewedPolicies(policyListResponseAssembler.summarize(recentViewedPolicies, likedPolicyIds))
                .popularPolicies(policyListResponseAssembler.summarize(popularPolicies, likedPolicyIds))
                .upcomingDeadlinePolicies(policyListResponseAssembler.summarize(upcomingDeadlinePolicies, likedPolicyIds))
                .latestPolicies(policyListResponseAssembler.summarize(latestPolicies, likedPolicyIds))
                .build();
    }

    public PolicyListResponse getRecentViewedPolicies(User user, String cursor, int size) {
        RecentViewedPolicyCursor decodedCursor = RecentViewedPolicyCursorParser.decode(cursor);
        List<RecentViewedPolicy> fetchedRecentViewedPolicies = fetchRecentViewedPolicies(
                user,
                decodedCursor,
                PageRequest.of(0, size + 1)
        );
        long totalCount = recentViewedPolicyRepository.countRecentViewedPolicies(
                user.getId(),
                RecruitmentStatus.CLOSED,
                user.getSidoCode(),
                user.getSigunguCode()
        );

        boolean hasNext = fetchedRecentViewedPolicies.size() > size;
        List<RecentViewedPolicy> recentViewedPolicies = hasNext
                ? fetchedRecentViewedPolicies.subList(0, size)
                : fetchedRecentViewedPolicies;
        List<Policy> policies = recentViewedPolicies.stream()
                .map(RecentViewedPolicy::getPolicy)
                .toList();
        Set<Long> likedPolicyIds = policyListResponseAssembler.findLikedPolicyIds(user.getId(), policies);
        String nextCursor = hasNext
                ? RecentViewedPolicyCursorParser.encode(recentViewedPolicies.getLast())
                : null;

        return PolicyListResponse.of(totalCount, policies, likedPolicyIds, nextCursor, hasNext);
    }

    private List<RecentViewedPolicy> fetchRecentViewedPolicies(
            User user,
            RecentViewedPolicyCursor cursor,
            PageRequest pageRequest
    ) {
        if (cursor != null) {
            return recentViewedPolicyRepository.findRecentViewedPoliciesAfter(
                    user.getId(),
                    RecruitmentStatus.CLOSED,
                    user.getSidoCode(),
                    user.getSigunguCode(),
                    cursor.viewedAt(),
                    cursor.policyId(),
                    pageRequest
            );
        }

        return recentViewedPolicyRepository.findRecentViewedPolicies(
                user.getId(),
                RecruitmentStatus.CLOSED,
                user.getSidoCode(),
                user.getSigunguCode(),
                pageRequest
        );
    }

    public PolicyListResponse getPolicies(
            User user,
            PolicyCategoryType category,
            PolicySortType sort,
            String cursor,
            int size
    ) {
        PolicyCursor decodedCursor = PolicyCursorParser.decode(cursor, sort);

        List<Policy> fetchedPolicies = fetchPoliciesByCategory(
                user,
                category,
                sort,
                decodedCursor,
                size + 1
        );
        long totalCount = category == null
                ? policyRepository.countVisiblePolicies(
                        RecruitmentStatus.CLOSED,
                        user.getSidoCode(),
                        user.getSigunguCode()
                )
                : policyRepository.countVisiblePoliciesByCategory(
                        category,
                        RecruitmentStatus.CLOSED,
                        user.getSidoCode(),
                        user.getSigunguCode()
                );

        return policyListResponseAssembler.assemble(user, sort, fetchedPolicies, totalCount, size);
    }

    public PolicyListResponse getLatestPolicies(
            User user,
            PolicyCategoryType category,
            String cursor,
            int size
    ) {
        return getPolicies(
                user,
                category,
                PolicySortType.LATEST,
                cursor,
                size
        );
    }

    public PolicyListResponse getUpcomingDeadlinePolicies(
            User user,
            PolicyCategoryType category,
            String cursor,
            int size
    ) {
        PolicyCursor decodedCursor = PolicyCursorParser.decode(cursor, PolicySortType.DEADLINE);

        if (decodedCursor != null && decodedCursor.applyEndDate() == null) {
            throw new PolicyException(PolicyErrorCode.INVALID_POLICY_CURSOR);
        }

        LocalDate today = LocalDate.now(SERVICE_ZONE_ID);
        List<Policy> fetchedPolicies = fetchUpcomingDeadlinePolicies(
                user,
                category,
                decodedCursor,
                today,
                PageRequest.of(0, size + 1)
        );
        long totalCount = category == null
                ? policyRepository.countVisibleUpcomingDeadlinePolicies(
                        RecruitmentStatus.CLOSED,
                        today,
                        user.getSidoCode(),
                        user.getSigunguCode()
                )
                : policyRepository.countVisibleUpcomingDeadlinePoliciesByCategory(
                        category,
                        RecruitmentStatus.CLOSED,
                        today,
                        user.getSidoCode(),
                        user.getSigunguCode()
                );

        return policyListResponseAssembler.assemble(
                user,
                PolicySortType.DEADLINE,
                fetchedPolicies,
                totalCount,
                size
        );
    }

    public PolicyListResponse getPopularPolicies(User user, PolicyCategoryType category, String cursor, int size) {

        //커서 파싱
        PolicyCursor decodedCursor = PolicyCursorParser.decode(cursor, PolicySortType.POPULAR);

        if (decodedCursor != null && decodedCursor.registeredAt() == null) {
            throw new PolicyException(PolicyErrorCode.INVALID_POLICY_CURSOR);
        }

        List<Policy> fetchedPolicies = fetchPopularPolicies(
                user,
                category,
                decodedCursor,
                PageRequest.of(0, size+1)
        );
        Long totalCount = category == null
                ? policyRepository.countVisiblePolicies(
                        RecruitmentStatus.CLOSED,
                        user.getSidoCode(),
                        user.getSigunguCode()
                )
                : policyRepository.countVisiblePoliciesByCategory(
                        category,
                        RecruitmentStatus.CLOSED,
                        user.getSidoCode(),
                        user.getSigunguCode()
                );

        //dto 변환
        return policyListResponseAssembler.assemble(
                user,
                PolicySortType.POPULAR,
                fetchedPolicies,
                totalCount,
                size
        );
    }

    private List<Policy> fetchPopularPolicies(User user, PolicyCategoryType category, PolicyCursor cursor, PageRequest pageRequest) {
        if (category == null) {
            if (cursor == null) {
                return policyRepository.findAllPopularPolicies(
                        RecruitmentStatus.CLOSED,
                        user.getSidoCode(),
                        user.getSigunguCode(),
                        pageRequest
                );
            }
            return policyRepository.findAllPopularPoliciesAfter(
                    RecruitmentStatus.CLOSED,
                    user.getSidoCode(),
                    user.getSigunguCode(),
                    cursor.registeredAt(),
                    cursor.popularityScore(),
                    cursor.policyId(),
                    pageRequest
            );
        }

        if (cursor == null) {
            return policyRepository.findPopularPolicies(
                    category,
                    RecruitmentStatus.CLOSED,
                    user.getSidoCode(),
                    user.getSigunguCode(),
                    pageRequest
            );
        }
        return policyRepository.findPopularPoliciesAfter(
                category,
                RecruitmentStatus.CLOSED,
                user.getSidoCode(),
                user.getSigunguCode(),
                cursor.registeredAt(),
                cursor.popularityScore(),
                cursor.policyId(),
                pageRequest
        );
    }

    private List<Policy> fetchPoliciesByCategory(
            User user,
            PolicyCategoryType category,
            PolicySortType sort,
            PolicyCursor cursor,
            int fetchSize
    ) {
        PageRequest pageRequest = PageRequest.of(0, fetchSize);
        if (sort == PolicySortType.LATEST) {
            return fetchLatestPolicies(user, category, cursor, pageRequest);
        }
        return fetchDeadlinePolicies(user, category, cursor, pageRequest);
    }

    private List<Policy> fetchLatestPolicies(
            User user,
            PolicyCategoryType category,
            PolicyCursor cursor,
            PageRequest pageRequest
    ) {
        if (category == null) {
            if (cursor == null) {
                return policyRepository.findAllLatestPolicies(
                        RecruitmentStatus.CLOSED,
                        user.getSidoCode(),
                        user.getSigunguCode(),
                        pageRequest
                );
            }
            return policyRepository.findAllLatestPoliciesAfter(
                    RecruitmentStatus.CLOSED,
                    user.getSidoCode(),
                    user.getSigunguCode(),
                    cursor.registeredAt(),
                    cursor.policyId(),
                    pageRequest
            );
        }
        if (cursor == null) {
            return policyRepository.findLatestPolicies(
                    category,
                    RecruitmentStatus.CLOSED,
                    user.getSidoCode(),
                    user.getSigunguCode(),
                    pageRequest
            );
        }
        return policyRepository.findLatestPoliciesAfter(
                category,
                RecruitmentStatus.CLOSED,
                user.getSidoCode(),
                user.getSigunguCode(),
                cursor.registeredAt(),
                cursor.policyId(),
                pageRequest
        );
    }

    private List<Policy> fetchDeadlinePolicies(
            User user,
            PolicyCategoryType category,
            PolicyCursor cursor,
            PageRequest pageRequest
    ) {
        if (cursor == null) {
            return policyRepository.findDeadlinePolicies(
                    category,
                    RecruitmentStatus.CLOSED,
                    user.getSidoCode(),
                    user.getSigunguCode(),
                    pageRequest
            );
        }
        if (cursor.applyEndDate() == null) {
            return policyRepository.findDeadlinePoliciesAfterOpenEndedCursor(
                    category,
                    RecruitmentStatus.CLOSED,
                    user.getSidoCode(),
                    user.getSigunguCode(),
                    cursor.policyId(),
                    pageRequest
            );
        }
        return policyRepository.findDeadlinePoliciesAfterDatedCursor(
                category,
                RecruitmentStatus.CLOSED,
                user.getSidoCode(),
                user.getSigunguCode(),
                cursor.applyEndDate(),
                cursor.policyId(),
                pageRequest
        );
    }

    private List<Policy> fetchUpcomingDeadlinePolicies(
            User user,
            PolicyCategoryType category,
            PolicyCursor cursor,
            LocalDate today,
            PageRequest pageRequest
    ) {
        if (category == null) {
            if (cursor == null) {
                return policyRepository.findAllUpcomingDeadlinePolicies(
                        RecruitmentStatus.CLOSED,
                        today,
                        user.getSidoCode(),
                        user.getSigunguCode(),
                        pageRequest
                );
            }
            return policyRepository.findAllUpcomingDeadlinePoliciesAfter(
                    RecruitmentStatus.CLOSED,
                    today,
                    user.getSidoCode(),
                    user.getSigunguCode(),
                    cursor.applyEndDate(),
                    cursor.policyId(),
                    pageRequest
            );
        }

        if (cursor == null) {
            return policyRepository.findUpcomingDeadlinePolicies(
                    category,
                    RecruitmentStatus.CLOSED,
                    today,
                    user.getSidoCode(),
                    user.getSigunguCode(),
                    pageRequest
            );
        }
        return policyRepository.findUpcomingDeadlinePoliciesAfter(
                category,
                RecruitmentStatus.CLOSED,
                today,
                user.getSidoCode(),
                user.getSigunguCode(),
                cursor.applyEndDate(),
                cursor.policyId(),
                pageRequest
        );
    }


}
