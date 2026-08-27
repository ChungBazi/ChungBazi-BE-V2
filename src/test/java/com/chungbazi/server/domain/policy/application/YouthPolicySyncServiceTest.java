package com.chungbazi.server.domain.policy.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.chungbazi.server.domain.policy.application.dto.PolicySyncItemResult;
import com.chungbazi.server.domain.policy.application.dto.PolicySyncStatus;
import com.chungbazi.server.domain.policy.application.dto.SyncResult;
import com.chungbazi.server.domain.policy.application.event.NewPoliciesRegisteredEvent;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.YouthPolicyClient;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.dto.YouthPolicyItem;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.dto.YouthPolicyListResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class YouthPolicySyncServiceTest {

    @Mock
    private YouthPolicyClient youthPolicyClient;

    @Mock
    private YouthPolicyPersistenceService youthPolicyPersistenceService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private YouthPolicySyncService youthPolicySyncService;

    @Test
    void publishesInsertedPolicyIdsAsOneEventAfterSynchronization() {
        YouthPolicyItem firstItem = org.mockito.Mockito.mock(YouthPolicyItem.class);
        YouthPolicyItem secondItem = org.mockito.Mockito.mock(YouthPolicyItem.class);
        given(youthPolicyClient.fetchPolicies(1, 100))
                .willReturn(response(List.of(firstItem, secondItem)));
        given(youthPolicyPersistenceService.syncPolicy(firstItem))
                .willReturn(PolicySyncItemResult.of(PolicySyncStatus.INSERTED, 10L));
        given(youthPolicyPersistenceService.syncPolicy(secondItem))
                .willReturn(PolicySyncItemResult.of(PolicySyncStatus.UPDATED, 20L));

        SyncResult result = youthPolicySyncService.syncPolicies();

        assertThat(result.insertedCount()).isEqualTo(1);
        assertThat(result.updatedCount()).isEqualTo(1);

        ArgumentCaptor<NewPoliciesRegisteredEvent> eventCaptor =
                ArgumentCaptor.forClass(NewPoliciesRegisteredEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().policyIds()).containsExactly(10L);
    }

    @Test
    void doesNotPublishEventWhenNoPolicyWasInserted() {
        YouthPolicyItem item = org.mockito.Mockito.mock(YouthPolicyItem.class);
        given(youthPolicyClient.fetchPolicies(1, 100))
                .willReturn(response(List.of(item)));
        given(youthPolicyPersistenceService.syncPolicy(item))
                .willReturn(PolicySyncItemResult.of(PolicySyncStatus.UNCHANGED, 10L));

        youthPolicySyncService.syncPolicies();

        verify(eventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }

    private YouthPolicyListResponse response(List<YouthPolicyItem> items) {
        return new YouthPolicyListResponse(
                0,
                "success",
                new YouthPolicyListResponse.Result(
                        new YouthPolicyListResponse.Paging(items.size(), 1, 100),
                        items
                )
        );
    }
}
