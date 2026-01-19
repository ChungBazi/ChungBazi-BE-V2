package chungbazi.chungbazi_be.domain.policy.repository;

import chungbazi.chungbazi_be.domain.policy.dto.PolicyListOneResponse;
import chungbazi.chungbazi_be.domain.policy.dto.PolicySearchResult;
import chungbazi.chungbazi_be.domain.policy.entity.Category;
import chungbazi.chungbazi_be.domain.policy.entity.Policy;
import chungbazi.chungbazi_be.domain.policy.entity.QPolicy;
import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import chungbazi.chungbazi_be.global.apiPayload.exception.GeneralException;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PolicyRepositoryImpl implements PolicyRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    QPolicy policy = QPolicy.policy;

    //정책 검색
    @Override
    public List<PolicySearchResult> searchPolicyWithName(String keyword, String cursor, int size, String order) {

        // 우선순위 점수 계산
        NumberExpression<Integer> priorityScore = calculateSearchPriority(keyword);

        List<Tuple> policies = jpaQueryFactory
                .select(policy.id,
                        policy.name,
                        policy.startDate,
                        policy.endDate,
                        policy.employment,
                        priorityScore)
                .from(policy)
                .where(searchName(keyword, policy), buildSearchCursor(cursor, keyword, policy, order))
                .orderBy(searchOrderSpecifiers(priorityScore, order, policy))
                .limit(size)
                .fetch();

        return convertToSearchResults(policies, priorityScore);
    }

    // 카테고리별 정책 조회
    @Override
    public List<PolicyListOneResponse> getPolicyWithCategory(Category category, String cursor, int size, String order) {

        List<Tuple> policies = jpaQueryFactory
                .select(
                        policy.id,
                        policy.name,
                        policy.startDate,
                        policy.endDate,
                        policy.employment)
                .from(policy)
                .where(eqCategory(category, policy), buildCategoryCursor(cursor, policy, order))
                .orderBy(orderSpecifiers(order, policy))
                .limit(size)
                .fetch();

        return convertToPolicyResponses(policies);
    }


    // ==================== 커서 생성 ====================

    @Override
    public String generateSearchCursor(PolicySearchResult result, String order) {
        if ("deadline".equals(order)) {
            // "우선순위-마감일-ID"
            String endDate = formatDate(result.getEndDate());
            return result.getPriorityScore() + "-" + endDate + "-" + result.getId();
        } else {
            // "우선순위-시작일-ID"
            String startDate = formatDate(result.getStartDate());
            return result.getPriorityScore() + "-" + startDate + "-" + result.getId();
        }
    }

    @Override
    public String generateCategoryCursor(PolicyListOneResponse policy, String order) {
        if ("deadline".equals(order)) {
            // "마감일-ID"
            String endDate = formatDate(policy.getEndDate());
            return endDate + "-" + policy.getPolicyId();
        } else {
            // "시작일-ID"
            String startDate = formatDate(policy.getStartDate());
            return startDate + "-" + policy.getPolicyId();
        }
    }

    // ==================== 커서 조건 빌더 ====================

    /*
        검색용 커서 조건 생성
        - 최신순: "우선순위-시작일-ID" (ex. "3-2026-03-01-1234")
        - 마감순: "우선순위-마감일-ID" (ex. "3-2026-12-31-1234")
    */
    private BooleanExpression buildSearchCursor(String cursor, String keyword, QPolicy policy, String order) {

        //cursor가 null일 경우 바로 return
        if (isNullOrEmpty(cursor)) {
            return null;
        }

        String[] cursorParts = cursor.split("-");

        NumberExpression<Integer> priorityScore = calculateSearchPriority(keyword);

        int cursorPriority = Integer.parseInt(cursorParts[0]);
        Long cursorId = Long.parseLong(cursorParts[4]);

        if ("deadline".equals(order)) {
            //우선순위-마감일-ID 형태의 cursor 파싱
            LocalDate cursorEndDate = parseDate(cursorParts[1], cursorParts[2], cursorParts[3]);

            return buildSearchDeadLineCursor(priorityScore, cursorPriority, cursorEndDate, cursorId);

        } else {
            //우선순위-시작일-ID 형태의 cursor 파싱
            LocalDate cursorStartDate = parseDate(cursorParts[1], cursorParts[2], cursorParts[3]);

            return buildSearchLatestCursor(priorityScore, cursorPriority, cursorStartDate, cursorId);
        }
    }

    private BooleanExpression buildSearchLatestCursor(NumberExpression<Integer> priorityScore, int cursorPriority, LocalDate cursorStartDate, Long cursorId) {

        //검색 우선순위가 더 낮은 정책들
        BooleanExpression lowerSearchPriority = priorityScore.lt(cursorPriority);

        //같은 검색 우선순위 + 시작일이 더 이전인 정책들
        BooleanExpression sameSearchPriorityLaterEndDate = priorityScore.eq(cursorPriority)
                .and(policy.startDate.lt(cursorStartDate));

        //같은 검색 우선순위 + 같은 마감일 + ID가 더 작은 정책들
        BooleanExpression allSameLowerId = priorityScore.eq(priorityScore)
                .and(policy.startDate.eq(cursorStartDate))
                .and(policy.id.lt(cursorId));

        return lowerSearchPriority
                .or(sameSearchPriorityLaterEndDate
                .or(allSameLowerId));

    }

    private BooleanExpression buildSearchDeadLineCursor(NumberExpression<Integer> priorityScore, int cursorPriority, LocalDate cursorEndDate, Long cursorId) {

        //검색 우선순위가 더 낮은 정책들
        BooleanExpression lowerSearchPriority = priorityScore.lt(cursorPriority);

        //같은 검색 우선순위 + 마감일이 더 나중인 정책들
        BooleanExpression sameSearchPriorityLaterEndDate = priorityScore.eq(cursorPriority)
                .and(policy.endDate.gt(cursorEndDate));

        //같은 검색 우선순위 + 같은 마감일 + ID가 더 작은 정책들
        BooleanExpression allSameLowerId = priorityScore.eq(cursorPriority)
                .and(policy.endDate.eq(cursorEndDate))
                .and(policy.id.lt(cursorId));

        return lowerSearchPriority
                .or(sameSearchPriorityLaterEndDate)
                .or(allSameLowerId);
    }

    /*
        카테고리용 커서 조건 생성
        - latest: "시작일-ID"
        - deadline: "마감일-ID"
    */
    private BooleanExpression buildCategoryCursor(String cursor, QPolicy policy, String order){
        if (isNullOrEmpty(cursor)) {
            return null;
        }

        String[] parts = cursor.split("-");
        Long cursorId = Long.parseLong(parts[3]);

        if ("deadline".equals(order)) {
            // "마감일-ID" 파싱
            LocalDate cursorEndDate = parseDate(parts[0], parts[1], parts[2]);

            return buildCategoryDeadlineCursor(cursorEndDate, cursorId);

        } else {
            // "시작일-ID" 파싱
            LocalDate cursorStartDate = parseDate(parts[0], parts[1], parts[2]);

            return buildCategoryLatestCursor(cursorStartDate, cursorId);
        }


    }

    private BooleanExpression buildCategoryDeadlineCursor(LocalDate cursorEndDate, Long cursorId) {

        //마감일이 더 나중인 정책들
        BooleanExpression laterEndDate = policy.endDate.gt(cursorEndDate);

        //같은 마감일 + ID가 더 작은 정책들
        BooleanExpression sameEndDateLowerId = policy.endDate.eq(cursorEndDate)
                .and(policy.id.lt(cursorId));

        return laterEndDate
                .or(sameEndDateLowerId);
    }

    private BooleanExpression buildCategoryLatestCursor(LocalDate cursorStartDate, Long cursorId) {

        //시작일이 더 이전인 정책들
        BooleanExpression earlierDate = policy.startDate.lt(cursorStartDate);

        //같은 시작일 + ID가 더 작은 정책들
        BooleanExpression sameStartDateLowerId = policy.startDate.eq(cursorStartDate)
                .and(policy.id.lt(cursorId));

        return earlierDate
                .or(sameStartDateLowerId);
    }

    // ==================== 정렬 순서 빌더 ====================

    //검색용 정렬
    private OrderSpecifier<?>[] searchOrderSpecifiers(NumberExpression<Integer> priorityScore, String order, QPolicy policy) {

        List<OrderSpecifier<?>> orderList = new ArrayList<>();

        // 1순위: 검색어 일치도 (항상 적용)
        orderList.add(new OrderSpecifier<>(Order.DESC, priorityScore));

        // 2순위: 사용자가 선택한 정렬 방식
        if ("deadline".equals(order)) {
            // 마감순 정렬
            orderList.add(new OrderSpecifier<>(Order.ASC, policy.endDate));

        } else {
            // 최신순 정렬 (기본값)
            orderList.add(new OrderSpecifier<>(Order.DESC, policy.startDate));
        }

        // 3순위: ID 내림차순
        orderList.add(new OrderSpecifier<>(Order.DESC, policy.id));

        return orderList.toArray(new OrderSpecifier[0]);
    }

    // 카테고리용 정렬
    private OrderSpecifier<?>[] orderSpecifiers(String order, QPolicy policy) {

        List<OrderSpecifier<?>> orderList = new ArrayList<>();
        // 마감순
        if ("deadline".equals(order)) {
            orderList.add(new OrderSpecifier<>(Order.ASC, policy.endDate));// 2.가까운 날짜

        } else { // 최신순, 디폴트
            orderList.add(new OrderSpecifier<>(Order.DESC, policy.startDate));
        }

        // 3순위: ID 내림차순 (동일 조건일 때 안정적인 정렬 보장)
        orderList.add(new OrderSpecifier<>(Order.DESC, policy.id));

        return orderList.toArray(new OrderSpecifier[0]);
    }

    // ==================== 검색조건 + category 필터링 ====================

    //카테고리 필터링
    private BooleanExpression eqCategory(Category category, QPolicy policy) {

        if (category == null) {
            return null;
        }

        return policy.category.eq(category);
    }

    // 이름 검색
    private BooleanExpression searchName(String name, QPolicy policy) {

        if (name == null) {
            throw new GeneralException(ErrorStatus.NO_SEARCH_NAME);
        }

        return policy.name.contains(name);
    }

    // ==================== DTO 변환 ====================

    private List<PolicySearchResult> convertToSearchResults(List<Tuple> tuples, NumberExpression<Integer> priorityScore) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        return tuples.stream()
                .map(tuple -> {
                    LocalDate endDate = tuple.get(policy.endDate);
                    Integer dDay = calculateDDay(endDate, today);

                    return PolicySearchResult.builder()
                            .id(tuple.get(policy.id))
                            .name(tuple.get(policy.name))
                            .startDate(tuple.get(policy.startDate))
                            .endDate(tuple.get(policy.endDate))
                            .dDay(dDay)
                            .employment(tuple.get(policy.employment))
                            .priorityScore(tuple.get(priorityScore))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<PolicyListOneResponse> convertToPolicyResponses(List<Tuple> tuples) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        return tuples.stream()
                .map(tuple -> {
                    LocalDate endDate = tuple.get(policy.endDate);
                    Integer dDay = calculateDDay(endDate, today);

                    return PolicyListOneResponse.builder()
                            .policyId(tuple.get(policy.id))
                            .policyName(tuple.get(policy.name))
                            .startDate(tuple.get(policy.startDate))
                            .endDate(endDate)
                            .dDay(dDay)
                            .employment(tuple.get(policy.employment))
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ==================== 유틸리티 메서드 ====================

    // 일치도 계산
    private NumberExpression<Integer> calculateSearchPriority(String keyword) {

        if (keyword == null) {
            throw new GeneralException(ErrorStatus.NO_SEARCH_NAME);
        }
        return new CaseBuilder()
                .when(QPolicy.policy.name.eq(keyword)).then(4) //완전 일치할 경우
                .when(QPolicy.policy.name.startsWith(keyword)).then(3) //시작이 일치할 경우
                .when(QPolicy.policy.name.contains(keyword)).then(2) //부분 일치할 경우
                .otherwise(1); //기타
    }

    private BooleanExpression eqCategory(Category category) {
        return category != null ? policy.category.eq(category) : null;
    }

    private BooleanExpression ltCursorId(Long cursor) {
        return (cursor != null && cursor != 0) ? policy.id.lt(cursor) : null;
    }

    private Integer calculateDDay(LocalDate endDate, LocalDate today) {
        return endDate != null ? (int) ChronoUnit.DAYS.between(today, endDate) : null;
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.toString() : "9999-12-31";
    }

    private LocalDate parseDate(String year, String month, String day) {
        return LocalDate.parse(year + "-" + month + "-" + day);
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty() || str.equals("\"\"");
    }

    private void validateKeyword(String keyword) {
        if (keyword == null) {
            throw new GeneralException(ErrorStatus.NO_SEARCH_NAME);
        }
    }
}