package com.chungbazi.server.domain.policy.service;

import com.chungbazi.server.domain.policy.client.YouthPolicyClient;
import com.chungbazi.server.domain.policy.client.dto.YouthPolicyItem;
import com.chungbazi.server.domain.policy.client.dto.YouthPolicyListResponse;
import java.util.List;

import com.chungbazi.server.domain.policy.dto.internal.PageSyncResult;
import com.chungbazi.server.domain.policy.dto.internal.SyncResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class YouthPolicySyncService {

    private static final int PAGE_SIZE = 10;
    private static final int FIRST_PAGE = 1;

    private final YouthPolicyClient youthPolicyClient;
    private final YouthPolicyPersistenceService youthPolicyPersistenceService;

    public SyncResult syncPolicies() {
        int pageNum = FIRST_PAGE;
        int totalFetchedCount = 0;
        int savedCount = 0;
        int skippedCount = 0;

        while (true) {
            YouthPolicyListResponse response = youthPolicyClient.fetchPolicies(pageNum, PAGE_SIZE);
            List<YouthPolicyItem> items = extractItems(response);

            if (items.isEmpty()) {
                break;
            }

            //정책 파싱 후 저장하기
            PageSyncResult pageSyncResult = syncPageItems(items);
            totalFetchedCount += pageSyncResult.fetchedCount();
            savedCount += pageSyncResult.savedCount();
            skippedCount += pageSyncResult.skippedCount();

            //새로운 정책이 없다면 정책 불러오기 stop
            if (hasNoNewPolicies(pageSyncResult)) {
                break;
            }

            //모든 페이지를 순회한 경우
            if (!hasNextPage(response, pageNum, items.size())) {
                break;
            }
            pageNum++;
        }

        return new SyncResult(totalFetchedCount, savedCount, skippedCount);
    }

    //정책 저장
    private PageSyncResult syncPageItems(List<YouthPolicyItem> items) {
        int savedCount = 0;
        int skippedCount = 0;

        //추후 배치처리?
        for (YouthPolicyItem item : items) {
            boolean saved = youthPolicyPersistenceService.saveIfNew(item);
            savedCount += saved ? 1 : 0;
            skippedCount += saved ? 0 : 1;
        }

        return new PageSyncResult(items.size(), savedCount, skippedCount);
    }

    //새로운 정책이 없는 경우
    private boolean hasNoNewPolicies(PageSyncResult pageSyncResult) {
        return pageSyncResult.savedCount() == 0;
    }

    private List<YouthPolicyItem> extractItems(YouthPolicyListResponse response) {
        if (response == null || response.result() == null || response.result().youthPolicyList() == null) {
            return List.of();
        }
        return response.result().youthPolicyList();
    }

    private boolean hasNextPage(YouthPolicyListResponse response, int pageNum, int itemCount) {
        YouthPolicyListResponse.Paging paging = response.result() == null ? null : response.result().pagging();
        if (paging == null || paging.totCount() == null) {
            return itemCount == PAGE_SIZE;
        }
        return pageNum * PAGE_SIZE < paging.totCount();
    }

}
