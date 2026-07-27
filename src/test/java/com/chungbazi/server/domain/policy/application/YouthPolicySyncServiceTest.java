package com.chungbazi.server.domain.policy.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.chungbazi.server.domain.policy.application.dto.PolicySyncStatus;
import com.chungbazi.server.domain.policy.application.dto.SyncResult;
import com.chungbazi.server.domain.policy.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.exception.PolicyException;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.YouthPolicyClient;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.dto.YouthPolicyItem;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.dto.YouthPolicyListResponse;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class YouthPolicySyncServiceTest {

    @Test
    void skipsInvalidRegionPolicyAndContinuesSync() {
        YouthPolicySyncService service = new YouthPolicySyncService(
                new FakeYouthPolicyClient(List.of(List.of(
                        item("saved-policy"),
                        item("old-policy"),
                        item("invalid-region-policy")
                ))),
                new FakeYouthPolicyPersistenceService()
        );

        SyncResult result = service.syncPolicies();

        assertThat(result.fetchedCount()).isEqualTo(3);
        assertThat(result.insertedCount()).isEqualTo(1);
        assertThat(result.updatedCount()).isZero();
        assertThat(result.unchangedCount()).isEqualTo(1);
        assertThat(result.skippedCount()).isEqualTo(1);
    }

    @Test
    void skipsInvalidCategoryPolicyAndContinuesSync() {
        YouthPolicySyncService service = new YouthPolicySyncService(
                new FakeYouthPolicyClient(List.of(List.of(
                        item("saved-policy"),
                        item("invalid-category-policy")
                ))),
                new FakeYouthPolicyPersistenceService()
        );

        SyncResult result = service.syncPolicies();

        assertThat(result.fetchedCount()).isEqualTo(2);
        assertThat(result.insertedCount()).isEqualTo(1);
        assertThat(result.skippedCount()).isEqualTo(1);
    }

    @Test
    void syncsAllPagesUntilLastPage() {
        List<YouthPolicyItem> firstPageItems = new ArrayList<>();
        firstPageItems.add(item("saved-policy"));
        for (int i = 0; i < 49; i++) {
            firstPageItems.add(item("old-policy-" + i));
        }

        FakeYouthPolicyClient youthPolicyClient = new FakeYouthPolicyClient(List.of(
                firstPageItems,
                List.of(
                        item("updated-policy"),
                        item("closed-policy")
                )
        ));

        YouthPolicySyncService service = new YouthPolicySyncService(
                youthPolicyClient,
                new FakeYouthPolicyPersistenceService()
        );

        SyncResult result = service.syncPolicies();

        assertThat(youthPolicyClient.getFetchedPageNumbers()).containsExactly(1, 2);
        assertThat(result.fetchedCount()).isEqualTo(52);
        assertThat(result.insertedCount()).isEqualTo(1);
        assertThat(result.updatedCount()).isEqualTo(1);
        assertThat(result.unchangedCount()).isEqualTo(49);
        assertThat(result.skippedCount()).isEqualTo(1);
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

        private final List<List<YouthPolicyItem>> pages;
        private final List<Integer> fetchedPageNumbers = new ArrayList<>();

        private FakeYouthPolicyClient(List<List<YouthPolicyItem>> pages) {
            super(null, "");
            this.pages = pages;
        }

        @Override
        public YouthPolicyListResponse fetchPolicies(int pageNum, int pageSize) {
            fetchedPageNumbers.add(pageNum);
            List<YouthPolicyItem> items = pages.size() < pageNum ? List.of() : pages.get(pageNum - 1);

            return new YouthPolicyListResponse(
                    200,
                    "success",
                    new YouthPolicyListResponse.Result(
                            new YouthPolicyListResponse.Paging(totalCount(), pageNum, pageSize),
                            items
                    )
            );
        }

        private int totalCount() {
            return pages.stream()
                    .mapToInt(List::size)
                    .sum();
        }

        private List<Integer> getFetchedPageNumbers() {
            return fetchedPageNumbers;
        }
    }

    private static class FakeYouthPolicyPersistenceService extends YouthPolicyPersistenceService {

        private FakeYouthPolicyPersistenceService() {
            super(null, null, null, null, null, null);
        }

        @Override
        public PolicySyncStatus syncPolicy(YouthPolicyItem item) {
            if (item.plcyNo().startsWith("old-policy")) {
                return PolicySyncStatus.UNCHANGED;
            }
            return switch (item.plcyNo()) {
                case "saved-policy" -> PolicySyncStatus.INSERTED;
                case "updated-policy" -> PolicySyncStatus.UPDATED;
                case "closed-policy" -> PolicySyncStatus.SKIPPED_CLOSED;
                case "invalid-region-policy" -> throw new PolicyException(PolicyErrorCode.INVALID_POLICY_REGION);
                case "invalid-category-policy" -> throw new PolicyException(PolicyErrorCode.INVALID_POLICY_CATEGORY);
                default -> throw new IllegalArgumentException("Unexpected policy number: " + item.plcyNo());
            };
        }
    }
}
