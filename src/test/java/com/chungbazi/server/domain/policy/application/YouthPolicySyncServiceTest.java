package com.chungbazi.server.domain.policy.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.chungbazi.server.domain.policy.application.dto.PageSyncResult;
import com.chungbazi.server.domain.policy.application.dto.SyncResult;
import com.chungbazi.server.domain.policy.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.exception.PolicyException;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.YouthPolicyClient;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.dto.YouthPolicyItem;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.dto.YouthPolicyListResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class YouthPolicySyncServiceTest {

    @Test
    void skipsInvalidRegionPolicyAndContinuesSync() {
        YouthPolicySyncService service = new YouthPolicySyncService(
                new FakeYouthPolicyClient(List.of(
                        item("saved-policy"),
                        item("old-policy"),
                        item("invalid-region-policy")
                )),
                new FakeYouthPolicyPersistenceService()
        );

        SyncResult result = service.syncPolicies();

        assertThat(result.fetchedCount()).isEqualTo(3);
        assertThat(result.savedCount()).isEqualTo(1);
        assertThat(result.skippedCount()).isEqualTo(2);
    }

    @Test
    void doesNotTreatInvalidRegionPolicyAsNoNewPolicies() {
        YouthPolicySyncService service = new YouthPolicySyncService(
                new FakeYouthPolicyClient(List.of()),
                new FakeYouthPolicyPersistenceService()
        );

        Boolean result = ReflectionTestUtils.invokeMethod(
                service,
                "hasNoNewPolicies",
                new PageSyncResult(1, 0, 1, 1)
        );

        assertThat(result).isFalse();
    }

    @Test
    void treatsOnlyExistingOrClosedPoliciesAsNoNewPolicies() {
        YouthPolicySyncService service = new YouthPolicySyncService(
                new FakeYouthPolicyClient(List.of()),
                new FakeYouthPolicyPersistenceService()
        );

        Boolean result = ReflectionTestUtils.invokeMethod(
                service,
                "hasNoNewPolicies",
                new PageSyncResult(1, 0, 1, 0)
        );

        assertThat(result).isTrue();
    }

    private static YouthPolicyItem item(String policyNumber) {
        return new YouthPolicyItem(
                policyNumber,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private static class FakeYouthPolicyClient extends YouthPolicyClient {

        private final List<YouthPolicyItem> items;

        private FakeYouthPolicyClient(List<YouthPolicyItem> items) {
            super(null, "");
            this.items = items;
        }

        @Override
        public YouthPolicyListResponse fetchPolicies(int pageNum, int pageSize) {
            return new YouthPolicyListResponse(
                    200,
                    "success",
                    new YouthPolicyListResponse.Result(
                            new YouthPolicyListResponse.Paging(items.size(), pageNum, pageSize),
                            items
                    )
            );
        }
    }

    private static class FakeYouthPolicyPersistenceService extends YouthPolicyPersistenceService {

        private FakeYouthPolicyPersistenceService() {
            super(null, null, null, null, null, null);
        }

        @Override
        public boolean saveIfNew(YouthPolicyItem item) {
            return switch (item.plcyNo()) {
                case "saved-policy" -> true;
                case "old-policy" -> false;
                case "invalid-region-policy" -> throw new PolicyException(PolicyErrorCode.INVALID_POLICY_REGION);
                default -> throw new IllegalArgumentException("Unexpected policy number: " + item.plcyNo());
            };
        }
    }
}
