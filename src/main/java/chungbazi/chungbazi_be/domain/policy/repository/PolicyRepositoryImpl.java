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

    @Override
    public List<PolicySearchResult> searchPolicyWithName(String keyword, String cursor, int size, String order) {

        // 우선순위 점수 계산
        NumberExpression<Integer> priorityScore = matchNamePriority(keyword);

        List<Tuple> policies = jpaQueryFactory
                .select(policy.id,
                        policy.name,
                        policy.startDate,
                        policy.endDate,
                        policy.employment,
                        priorityScore)
                .from(policy)
                .where(searchName(keyword, policy), ltCursor(cursor, keyword, policy))
                .orderBy(searchOrderSpecifiers(priorityScore, order, policy))
                .limit(size)
                .fetch();

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        return policies.stream()
                .map(tuple -> {
                    LocalDate endDate = tuple.get(policy.endDate);
                    Integer dDay = endDate != null
                            ? (int) ChronoUnit.DAYS.between(today, endDate)
                            : null;

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

    // 이번에 조회된 마지막 커서 값 반환 (우선순위+id)
    @Override
    public String generateNextCursor(Tuple tuple, String name) {

        Policy policy = tuple.get(QPolicy.policy);
        Integer priority = tuple.get(matchNamePriority(name));

        if (policy == null) {
            throw new GeneralException(ErrorStatus.POLICY_NOT_FOUND);
        }
        Long policyId = policy.getId();

        return priority + "-" + policyId;
    }

    // 카테고리별 정책 조회
    @Override
    public List<PolicyListOneResponse> getPolicyWithCategory(Category category, Long cursor, int size, String order) {

        List<PolicyListOneResponse> policies = jpaQueryFactory
                .select(Projections.constructor(PolicyListOneResponse.class,
                        policy.id,
                        policy.name,
                        policy.startDate,
                        policy.endDate,
                        policy.employment))
                .from(policy)
                .where(eqCategory(category, policy), ltCursorId(cursor, policy))
                .orderBy(orderSpecifiers(order, policy))
                .limit(size)
                .fetch();

        return policies;
    }

    @Override
    public List<PolicyListOneResponse> findByCategory(Category category, Long cursor, int size, String order) {

        return jpaQueryFactory
                .select(Projections.constructor(PolicyListOneResponse.class,
                        policy.id,
                        policy.name,
                        policy.startDate,
                        policy.endDate,
                        policy.employment))
                .from(policy)
                .where(
                        policy.category.eq(category), // 정책 카테고리 필터링
                        ltCursorId(cursor, policy)
                )
                .orderBy(orderSpecifiers(order, policy))
                .limit(size)
                .fetch();
    }


    // 이름 검색
    private BooleanExpression searchName(String name, QPolicy policy) {

        if (name == null) {
            throw new GeneralException(ErrorStatus.NO_SEARCH_NAME);
        }

        return policy.name.contains(name);
    }


    // 일치도 순 정렬, 일치도 같다면 Id 크기로 정렬
    private BooleanExpression ltCursor(String cursor, String keyword, QPolicy policy) {

        if (cursor == null || cursor.isEmpty() || cursor.equals("\"\"")) {
            return null;
        }

        String[] cursorParts = cursor.split("-");
        int cursorMatchScore = Integer.parseInt(cursorParts[0]);
        Long policyId = Long.parseLong(cursorParts[1]);

        NumberExpression<Integer> priorityScore = matchNamePriority(keyword);

        // cursor priority보다 우선순위가 낮은 애들
        BooleanExpression matchScoreCondition = priorityScore.lt(cursorMatchScore);

        //cursor priority와 우선순위 같은 애들
        BooleanExpression SameScoreCondition = priorityScore.eq(cursorMatchScore)
                .and(policy.id.lt(policyId));

        // matchScoreCondition 이거나 SameScoreCondition
        return matchScoreCondition.or(SameScoreCondition);
    }

    // 일치도 계산
    private NumberExpression<Integer> matchNamePriority(String keyword) {

        if (keyword == null) {
            throw new GeneralException(ErrorStatus.NO_SEARCH_NAME);
        }
        return new CaseBuilder()
                .when(QPolicy.policy.name.eq(keyword)).then(4) //완전 일치할 경우
                .when(QPolicy.policy.name.startsWith(keyword)).then(3) //시작이 일치할 경우
                .when(QPolicy.policy.name.contains(keyword)).then(2) //부분 일치할 경우
                .otherwise(1); //기타
    }

    private OrderSpecifier<?>[] searchOrderSpecifiers(NumberExpression<Integer> priorityScore, String order, QPolicy policy) {

        LocalDate today = LocalDate.now();
        List<OrderSpecifier<?>> orderList = new ArrayList<>();

        // 1순위: 검색어 일치도 (항상 적용)
        orderList.add(new OrderSpecifier<>(Order.DESC, priorityScore));

        // 2순위: 사용자가 선택한 정렬 방식
        if ("deadline".equals(order)) {
            // 마감순 정렬
            NumberExpression<Integer> deadlinePriority = new CaseBuilder()
                    .when(policy.endDate.goe(today)).then(0)  // 마감 안 지남
                    .otherwise(1); // 마감 지남

            orderList.add(new OrderSpecifier<>(Order.ASC, deadlinePriority));
            orderList.add(new OrderSpecifier<>(Order.ASC, policy.endDate));

        } else {
            // 최신순 정렬 (기본값)
            orderList.add(new OrderSpecifier<>(Order.DESC, policy.startDate));
        }

        // 3순위: ID 내림차순 (동일 조건일 때 안정적인 정렬 보장)
        orderList.add(new OrderSpecifier<>(Order.DESC, policy.id));

        return orderList.toArray(new OrderSpecifier[0]);
    }

    // 정렬 방법
    private OrderSpecifier<?>[] orderSpecifiers(String order, QPolicy policy) {

        LocalDate today = LocalDate.now();

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

    private BooleanExpression ltCursorId(Long cursor, QPolicy policy) {

        if (cursor == null || cursor == 0) {
            return null;
        }

        return policy.id.lt(cursor);
    }

    private BooleanExpression eqCategory(Category category, QPolicy policy) {

        if (category == null) {
            return null;
        }

        return policy.category.eq(category);
    }
}