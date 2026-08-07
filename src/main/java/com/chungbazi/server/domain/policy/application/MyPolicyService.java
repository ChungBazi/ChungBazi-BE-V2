package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.api.dto.response.MyPolicyDeadlineResponse;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse.PolicySummary;
import com.chungbazi.server.domain.policy.application.cursor.PolicyCursor;
import com.chungbazi.server.domain.policy.application.cursor.PolicyCursorParser;
import com.chungbazi.server.domain.policy.application.mapper.PolicyListResponseAssembler;
import com.chungbazi.server.domain.policy.application.mapper.PolicyDisplayMapper;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.PolicyLike;
import com.chungbazi.server.domain.policy.domain.repository.PolicyLikeRepository;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.PolicyListSortType;
import com.chungbazi.server.domain.policy.domain.type.PolicySortType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.policy.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.exception.PolicyException;
import com.chungbazi.server.domain.user.domain.User;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPolicyService {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final int MAX_DEADLINE_SIZE = 5;

    private final PolicyLikeRepository policyLikeRepository;
    private final PolicyDisplayMapper policyDisplayMapper;
    private final PolicyListResponseAssembler policyListResponseAssembler;

    public MyPolicyDeadlineResponse getMyPolicyDeadline(User user) {
        LocalDate today = LocalDate.now(SERVICE_ZONE_ID);

        PageRequest pageRequest = PageRequest.of(0, MAX_DEADLINE_SIZE);

        List<Policy> policies = policyLikeRepository.findUpcomingDeadlineLikedPolicies(
                        user.getId(),
                        RecruitmentStatus.CLOSED,
                        today,
                        pageRequest
                );

        Set<Long> likedPolicyIds = policies.stream()
                .map(Policy::getId)
                .collect(Collectors.toSet());

        List<PolicySummary> policySummaries = policyDisplayMapper.toSummaries(policies, likedPolicyIds);
        return new MyPolicyDeadlineResponse(policySummaries);
    }

    public PolicyListResponse getDeadlinePoliciesByDate(User user, LocalDate targetDate, PolicyListSortType sort, String cursor, int size) {
        PolicySortType policySort = sort.getPolicySortType();

        PolicyCursor decodedCursor = PolicyCursorParser.decode(cursor, policySort);
        PageRequest pageRequest = PageRequest.of(0, size + 1);

        List<Policy> fetchedPolicies = fetchDeadlinePoliciesByDate(
                user.getId(),
                targetDate,
                policySort,
                decodedCursor,
                pageRequest
        );

        Long totalCount = policyLikeRepository.countDeadlineLikedPoliciesByDate(
                user.getId(),
                RecruitmentStatus.CLOSED,
                targetDate
        );

        return assembleLikedPolicyListResponse(
                fetchedPolicies,
                totalCount,
                size,
                policy -> encodeLikedPolicyCursor(policySort, policy)
        );
    }

    private List<Policy> fetchDeadlinePoliciesByDate(Long userId, LocalDate targetDate, PolicySortType sort, PolicyCursor cursor, PageRequest pageRequest) {
        if (sort == PolicySortType.DEADLINE) {
            if (cursor == null) {
                return policyLikeRepository.findDeadlineLikedPoliciesByDateOrderByDeadlineFirst(
                        userId,
                        RecruitmentStatus.CLOSED,
                        targetDate,
                        pageRequest
                );
            }

            return policyLikeRepository.findDeadlineLikedPoliciesByDateOrderByDeadlineAfter(
                    userId,
                    RecruitmentStatus.CLOSED,
                    targetDate,
                    cursor.policyId(),
                    pageRequest
            );
        }

        if (cursor == null) {
            return policyLikeRepository.findDeadlineLikedPoliciesByDateOrderByLatestFirst(
                    userId,
                    RecruitmentStatus.CLOSED,
                    targetDate,
                    pageRequest
            );
        }

        return policyLikeRepository.findDeadlineLikedPoliciesByDateOrderByLatestAfter(
                userId,
                RecruitmentStatus.CLOSED,
                targetDate,
                cursor.registeredAt(),
                cursor.policyId(),
                pageRequest
        );
    }

    public PolicyListResponse getOpenEndedLikedPolicies(User user, String cursor, int size) {
        Long decodedCursor = decodePolicyLikeIdCursor(cursor);
        PageRequest pageRequest = PageRequest.of(0, size + 1);

        List<PolicyLike> fetchedPolicyLikes = fetchOpenEndedLikedPolicies(
                user.getId(),
                decodedCursor,
                pageRequest
        );

        boolean hasNext = fetchedPolicyLikes.size() > size;
        List<PolicyLike> policyLikes = hasNext
                ? new ArrayList<>(fetchedPolicyLikes.subList(0, size))
                : fetchedPolicyLikes;

        List<Policy> policies = policyLikes.stream()
                .map(PolicyLike::getPolicy)
                .toList();

        String nextCursor = hasNext
                ? policyLikes.getLast().getId().toString()
                : null;

        Long totalCount = policyLikeRepository.countOpenEndedLikedPolicies(
                user.getId(),
                RecruitmentStatus.CLOSED,
                RecruitmentType.ALWAYS
        );

        return assembleLikedPolicyListResponse(policies, totalCount, nextCursor, hasNext);
    }

    private List<PolicyLike> fetchOpenEndedLikedPolicies(
            Long userId,
            Long cursor,
            PageRequest pageRequest
    ) {
        if (cursor == null) {
            return policyLikeRepository.findOpenEndedLikedPoliciesFirst(
                    userId,
                    RecruitmentStatus.CLOSED,
                    RecruitmentType.ALWAYS,
                    pageRequest
            );
        }

        return policyLikeRepository.findOpenEndedLikedPoliciesAfter(
                userId,
                RecruitmentStatus.CLOSED,
                RecruitmentType.ALWAYS,
                cursor,
                pageRequest
        );
    }

    private Long decodePolicyLikeIdCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        try {
            return Long.valueOf(cursor);
        } catch (NumberFormatException exception) {
            throw new PolicyException(PolicyErrorCode.INVALID_POLICY_CURSOR);
        }
    }

    public PolicyListResponse getMyPoliciesByCategory(
            User user,
            PolicyCategoryType category,
            PolicyListSortType sort,
            String cursor,
            int size
    ) {
        PolicySortType policySort = sort.getPolicySortType();
        PolicyCursor decodedCursor = PolicyCursorParser.decode(cursor, policySort);

        List<Policy> fetchedPolicies = fetchMyPoliciesByCategory(
                user.getId(),
                category,
                policySort,
                decodedCursor,
                PageRequest.of(0, size + 1)
        );
        Long totalCount = policyLikeRepository.countMyLikedPolicies(
                user.getId(),
                RecruitmentStatus.CLOSED,
                category
        );

        return assembleLikedPolicyListResponse(
                fetchedPolicies,
                totalCount,
                size,
                policy -> encodeLikedPolicyCursor(policySort, policy)
        );
    }

    private PolicyListResponse assembleLikedPolicyListResponse(
            List<Policy> fetchedPolicies,
            Long totalCount,
            int size,
            Function<Policy, String> nextCursorEncoder
    ) {
        boolean hasNext = fetchedPolicies.size() > size;
        List<Policy> policies = hasNext
                ? new ArrayList<>(fetchedPolicies.subList(0, size))
                : fetchedPolicies;

        String nextCursor = hasNext
                ? nextCursorEncoder.apply(policies.getLast())
                : null;

        return assembleLikedPolicyListResponse(policies, totalCount, nextCursor, hasNext);
    }

    private PolicyListResponse assembleLikedPolicyListResponse(
            List<Policy> policies,
            Long totalCount,
            String nextCursor,
            boolean hasNext
    ) {
        Set<Long> likedPolicyIds = policies.stream()
                .map(Policy::getId)
                .collect(Collectors.toSet());

        return policyListResponseAssembler.assemble(
                totalCount,
                policyListResponseAssembler.summarize(policies, likedPolicyIds),
                nextCursor,
                hasNext
        );
    }

    private List<Policy> fetchMyPoliciesByCategory(
            Long userId,
            PolicyCategoryType category,
            PolicySortType sort,
            PolicyCursor cursor,
            PageRequest pageRequest
    ) {
        if (sort == PolicySortType.LATEST) {
            return fetchMyPoliciesByLatest(userId, category, cursor, pageRequest);
        }

        return fetchMyPoliciesByDeadline(userId, category, cursor, pageRequest);
    }

    private List<Policy> fetchMyPoliciesByLatest(
            Long userId,
            PolicyCategoryType category,
            PolicyCursor cursor,
            PageRequest pageRequest
    ) {
        if (cursor == null) {
            return policyLikeRepository.findMyLikedPoliciesOrderByLatestFirst(
                    userId,
                    RecruitmentStatus.CLOSED,
                    category,
                    pageRequest
            );
        }

        return policyLikeRepository.findMyLikedPoliciesOrderByLatestAfter(
                userId,
                RecruitmentStatus.CLOSED,
                category,
                cursor.registeredAt(),
                cursor.policyId(),
                pageRequest
        );
    }

    private List<Policy> fetchMyPoliciesByDeadline(
            Long userId,
            PolicyCategoryType category,
            PolicyCursor cursor,
            PageRequest pageRequest
    ) {
        if (cursor == null) {
            return policyLikeRepository.findMyLikedPoliciesOrderByDeadlineFirst(
                    userId,
                    RecruitmentStatus.CLOSED,
                    category,
                    RecruitmentType.ALWAYS,
                    pageRequest
            );
        }

        if (cursor.applyEndDate() == null) {
            return policyLikeRepository.findMyLikedPoliciesOrderByDeadlineAfterOpenEndedCursor(
                    userId,
                    RecruitmentStatus.CLOSED,
                    category,
                    RecruitmentType.ALWAYS,
                    cursor.policyId(),
                    pageRequest
            );
        }

        return policyLikeRepository.findMyLikedPoliciesOrderByDeadlineAfterDatedCursor(
                userId,
                RecruitmentStatus.CLOSED,
                category,
                RecruitmentType.ALWAYS,
                cursor.applyEndDate(),
                cursor.policyId(),
                pageRequest
        );
    }

    private String encodeLikedPolicyCursor(PolicySortType sort, Policy policy) {
        if (sort == PolicySortType.DEADLINE && isOpenEndedPolicy(policy)) {
            return PolicyCursorParser.encodeOpenEndedDeadline(policy.getId());
        }

        return PolicyCursorParser.encode(sort, policy);
    }

    private boolean isOpenEndedPolicy(Policy policy) {
        return policy.getRecruitmentType() == RecruitmentType.ALWAYS
                || policy.getApplyEndDate() == null;
    }
}
