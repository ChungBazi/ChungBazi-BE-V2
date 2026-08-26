package com.chungbazi.server.domain.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.chungbazi.server.domain.notification.application.dto.PersonalizedPolicyNotificationTarget;
import com.chungbazi.server.domain.notification.application.mapper.PersonalizedPolicyContextMapper;
import com.chungbazi.server.domain.notification.application.mapper.PersonalizedPolicyNotificationMapper;
import com.chungbazi.server.domain.notification.domain.repository.NotificationRepository;
import com.chungbazi.server.domain.policy.application.dto.PolicyRecommendationContext;
import com.chungbazi.server.domain.policy.application.support.PersonalizedPolicyRanker;
import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.entity.PolicyRegion;
import com.chungbazi.server.domain.policy.domain.repository.PolicyRegionRepository;
import com.chungbazi.server.domain.policy.domain.repository.policyRepository.PolicyRepository;
import com.chungbazi.server.domain.policy.domain.type.SidoCode;
import com.chungbazi.server.domain.user.domain.User;
import com.chungbazi.server.domain.user.infrastructure.UserInterestRepository;
import com.chungbazi.server.domain.user.infrastructure.UserRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonalizedPolicyTargetSelectionTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserInterestRepository userInterestRepository;

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private PolicyRegionRepository policyRegionRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private PersonalizedPolicyContextMapper personalizedPolicyContextMapper;

    @Mock
    private PersonalizedPolicyNotificationMapper personalizedPolicyNotificationMapper;

    @Mock
    private PersonalizedPolicyRanker personalizedPolicyRanker;

    @InjectMocks
    private PersonalizedPolicyNotificationService personalizedPolicyNotificationService;

    @Test
    void filtersNewPoliciesByUserRegionAndSelectsUpToFiveWithTheExistingRanker() {
        User seoulUser = user(10L, SidoCode.SEOUL);
        User busanUser = user(20L, SidoCode.BUSAN);
        Policy nationalPolicy = policy(100L, true);
        Policy seoulPolicy = policy(200L, false);
        PolicyRegion seoulRegion = org.mockito.Mockito.mock(PolicyRegion.class);
        given(seoulRegion.getSidoCode()).willReturn(SidoCode.SEOUL);
        given(seoulRegion.getRegionCode()).willReturn(null);

        PolicyRecommendationContext seoulContext = emptyContext();
        PolicyRecommendationContext busanContext = emptyContext();
        List<Policy> newPolicies = List.of(nationalPolicy, seoulPolicy);
        Map<Long, List<PolicyRegion>> regions = Map.of(200L, List.of(seoulRegion));

        given(personalizedPolicyRanker.rank(
                seoulUser,
                seoulContext,
                List.of(nationalPolicy, seoulPolicy),
                5
        )).willReturn(List.of(seoulPolicy, nationalPolicy));
        given(personalizedPolicyRanker.rank(
                busanUser,
                busanContext,
                List.of(nationalPolicy),
                5
        )).willReturn(List.of(nationalPolicy));

        List<PersonalizedPolicyNotificationTarget> targets =
                personalizedPolicyNotificationService.findPersonalizedTargets(
                        List.of(seoulUser, busanUser),
                        Map.of(10L, seoulContext, 20L, busanContext),
                        newPolicies,
                        regions
                );

        assertThat(targets).hasSize(2);
        assertThat(targets.get(0).user()).isEqualTo(seoulUser);
        assertThat(targets.get(0).policies()).containsExactly(seoulPolicy, nationalPolicy);
        assertThat(targets.get(1).user()).isEqualTo(busanUser);
        assertThat(targets.get(1).policies()).containsExactly(nationalPolicy);
        verify(personalizedPolicyRanker).rank(
                seoulUser,
                seoulContext,
                List.of(nationalPolicy, seoulPolicy),
                5
        );
        verify(personalizedPolicyRanker).rank(
                busanUser,
                busanContext,
                List.of(nationalPolicy),
                5
        );
    }

    private User user(Long id, SidoCode sidoCode) {
        User user = org.mockito.Mockito.mock(User.class);
        given(user.getId()).willReturn(id);
        given(user.getSidoCode()).willReturn(sidoCode);
        return user;
    }

    private Policy policy(Long id, boolean national) {
        Policy policy = org.mockito.Mockito.mock(Policy.class);
        given(policy.getId()).willReturn(id);
        given(policy.isNational()).willReturn(national);
        return policy;
    }

    private PolicyRecommendationContext emptyContext() {
        return PolicyRecommendationContext.of(List.of(), List.of(), List.of());
    }
}
