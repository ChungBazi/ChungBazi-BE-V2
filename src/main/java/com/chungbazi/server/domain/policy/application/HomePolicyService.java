package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.repository.PolicyLikeRepository;
import com.chungbazi.server.domain.policy.domain.repository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.PolicySortType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.policy.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.exception.PolicyException;
import com.chungbazi.server.domain.user.domain.User;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomePolicyService {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String CURSOR_SEPARATOR = "\\|";
    private static final String CURSOR_JOINER = "|";
    private static final String NULL_DATE = "NULL";

    private final PolicyRepository policyRepository;
    private final PolicyLikeRepository policyLikeRepository;

    public PolicyListResponse getPolicies(
            User user,
            PolicyCategoryType category,
            PolicySortType sort,
            String cursor,
            int size
    ) {
        Cursor decodedCursor = decodeCursor(cursor, sort);
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

        return createResponse(user, sort, fetchedPolicies, totalCount, size);
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
        Cursor decodedCursor = decodeCursor(cursor, PolicySortType.DEADLINE);
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

        return createResponse(
                user,
                PolicySortType.DEADLINE,
                fetchedPolicies,
                totalCount,
                size
        );
    }

    private PolicyListResponse createResponse(
            User user,
            PolicySortType sort,
            List<Policy> fetchedPolicies,
            long totalCount,
            int size
    ) {
        boolean hasNext = fetchedPolicies.size() > size;
        List<Policy> policies = hasNext
                ? new ArrayList<>(fetchedPolicies.subList(0, size))
                : fetchedPolicies;

        Set<Long> likedPolicyIds = findLikedPolicyIds(user.getId(), policies);
        String nextCursor = hasNext ? encodeCursor(sort, policies.getLast()) : null;

        return PolicyListResponse.of(
                totalCount,
                policies,
                likedPolicyIds,
                nextCursor,
                hasNext
        );
    }

    private List<Policy> fetchPoliciesByCategory(
            User user,
            PolicyCategoryType category,
            PolicySortType sort,
            Cursor cursor,
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
            Cursor cursor,
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
            Cursor cursor,
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
            Cursor cursor,
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

    private Set<Long> findLikedPolicyIds(Long userId, List<Policy> policies) {
        if (policies.isEmpty()) {
            return Set.of();
        }
        List<Long> policyIds = policies.stream()
                .map(Policy::getId)
                .toList();
        return new HashSet<>(policyLikeRepository.findLikedPolicyIds(userId, policyIds));
    }

    private String encodeCursor(PolicySortType sort, Policy policy) {
        String sortValue = sort.name();
        String dateValue = sort == PolicySortType.LATEST
                ? policy.getRegisteredAt().toString()
                : policy.getApplyEndDate() == null
                        ? NULL_DATE
                        : policy.getApplyEndDate().toString();
        String rawCursor = String.join(
                CURSOR_JOINER,
                sortValue,
                dateValue,
                policy.getId().toString()
        );
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(rawCursor.getBytes(StandardCharsets.UTF_8));
    }

    private Cursor decodeCursor(String encodedCursor, PolicySortType requestedSort) {
        if (encodedCursor == null || encodedCursor.isBlank()) {
            return null;
        }

        try {
            String rawCursor = new String(
                    Base64.getUrlDecoder().decode(encodedCursor),
                    StandardCharsets.UTF_8
            );
            String[] values = rawCursor.split(CURSOR_SEPARATOR, -1);
            if (values.length != 3 || !requestedSort.name().equals(values[0])) {
                throw new IllegalArgumentException();
            }

            Long policyId = Long.valueOf(values[2]);
            if (requestedSort == PolicySortType.LATEST) {
                return new Cursor(LocalDateTime.parse(values[1]), null, policyId);
            }
            LocalDate applyEndDate = NULL_DATE.equals(values[1])
                    ? null
                    : LocalDate.parse(values[1]);
            return new Cursor(null, applyEndDate, policyId);
        } catch (RuntimeException exception) {
            throw new PolicyException(PolicyErrorCode.INVALID_POLICY_CURSOR);
        }
    }

    private record Cursor(
            LocalDateTime registeredAt,
            LocalDate applyEndDate,
            Long policyId
    ) {
    }
}
