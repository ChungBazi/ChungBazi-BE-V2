package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.api.dto.response.MyPolicyDeadlineResponse;
import com.chungbazi.server.domain.policy.api.dto.response.PolicyListResponse.PolicySummary;
import com.chungbazi.server.domain.policy.application.mapper.PolicyDisplayMapper;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.repository.PolicyLikeRepository;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.user.domain.User;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
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
}
