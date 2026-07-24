package com.chungbazi.server.domain.policy.application;

import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.YouthPolicyClient;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.dto.YouthPolicyItem;
import com.chungbazi.server.domain.policy.infrastructure.external.youthpolicy.client.dto.YouthPolicyListResponse;
import java.util.List;

import com.chungbazi.server.domain.policy.application.dto.PageSyncResult;
import com.chungbazi.server.domain.policy.application.dto.SyncResult;
import com.chungbazi.server.domain.policy.exception.PolicyErrorCode;
import com.chungbazi.server.domain.policy.exception.PolicyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class YouthPolicySyncService {

    private static final int PAGE_SIZE = 50;
    private static final int FIRST_PAGE = 1;

    private final YouthPolicyClient youthPolicyClient;
    private final YouthPolicyPersistenceService youthPolicyPersistenceService;

    public SyncResult syncPolicies() {
        int pageNum = FIRST_PAGE;
        int totalFetchedCount = 0;
        int savedCount = 0;
        int skippedCount = 0;

        //TODO:스케줄링 로직 추후 구현
        //while (true) {
            YouthPolicyListResponse response = youthPolicyClient.fetchPolicies(pageNum, PAGE_SIZE);
            List<YouthPolicyItem> items = extractItems(response);

            if (items.isEmpty()) {
                //break;
            }

            //정책 파싱 후 저장하기
            PageSyncResult pageSyncResult = syncPageItems(items);
            totalFetchedCount += pageSyncResult.fetchedCount();
            savedCount += pageSyncResult.savedCount();
            skippedCount += pageSyncResult.skippedCount();

            //새로운 정책이 없다면 정책 불러오기 stop
            if (hasNoNewPolicies(pageSyncResult)) {
                //break;
            }

            //모든 페이지를 순회한 경우
            if (!hasNextPage(response, pageNum, items.size())) {
                //break;
            }
            pageNum++;
        //}

        return new SyncResult(totalFetchedCount, savedCount, skippedCount);
    }

    //정책 저장
    private PageSyncResult syncPageItems(List<YouthPolicyItem> items) {
        int savedCount = 0;
        int skippedCount = 0;
        int invalidRegionCount = 0;

        //추후 배치처리?
        for (YouthPolicyItem item : items) {
            try {
                boolean saved = youthPolicyPersistenceService.saveIfNew(item);
                if (!saved) {
                    skippedCount++;
                    continue;
                }
                savedCount++;
            } catch (PolicyException exception) {
                if (exception.getCode() != PolicyErrorCode.INVALID_POLICY_REGION) {
                    throw exception;
                }

                log.warn(
                        "유효하지않은 지역코드가 있습니다. plcyNo={}, zipCd={}",
                        item.plcyNo(),
                        item.zipCd()
                );
                skippedCount++;
                invalidRegionCount++;
            }
        }

        return new PageSyncResult(items.size(), savedCount, skippedCount, invalidRegionCount);
    }

    //새로운 정책이 없는 경우
    private boolean hasNoNewPolicies(PageSyncResult pageSyncResult) {
        return pageSyncResult.savedCount() == 0
                && pageSyncResult.invalidRegionCount() == 0;
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
