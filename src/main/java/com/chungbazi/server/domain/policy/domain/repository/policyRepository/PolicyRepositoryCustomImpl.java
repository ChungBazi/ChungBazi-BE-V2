package com.chungbazi.server.domain.policy.domain.repository.policyRepository;

import com.chungbazi.server.domain.policy.domain.entity.Policy;
import com.chungbazi.server.domain.policy.domain.type.PolicyCategoryType;
import com.chungbazi.server.domain.policy.domain.type.RecruitmentStatus;
import com.chungbazi.server.domain.policy.domain.type.SidoCode;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.chungbazi.server.domain.policy.domain.entity.QPolicy.policy;
import static com.chungbazi.server.domain.policy.domain.entity.QPolicyRegion.policyRegion;

@Repository
@RequiredArgsConstructor
public class PolicyRepositoryCustomImpl implements PolicyRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public long countVisiblePoliciesByCategory(PolicyCategoryType category,
                                               RecruitmentStatus closedStatus,
                                               SidoCode sidoCode,
                                               String sigunguCode) {
        return count(basePredicate(category, closedStatus, sidoCode, sigunguCode));
    }

    @Override
    public long countVisiblePolicies(RecruitmentStatus closedStatus,
                                     SidoCode sidoCode,
                                     String sigunguCode) {
        return count(basePredicate(null, closedStatus, sidoCode, sigunguCode));
    }

    @Override
    public long countVisibleUpcomingDeadlinePolicies(RecruitmentStatus closedStatus,
                                                     LocalDate today,
                                                     SidoCode sidoCode,
                                                     String sigunguCode) {
        return count(basePredicate(null, closedStatus, sidoCode, sigunguCode)
                .and(upcomingDeadline(today)));
    }

    @Override
    public long countVisibleUpcomingDeadlinePoliciesByCategory(PolicyCategoryType category,
                                                               RecruitmentStatus closedStatus,
                                                               LocalDate today,
                                                               SidoCode sidoCode,
                                                               String sigunguCode) {
        return count(basePredicate(category, closedStatus, sidoCode, sigunguCode)
                .and(upcomingDeadline(today)));
    }

    @Override
    public List<Policy> findAllUpcomingDeadlinePolicies(RecruitmentStatus closedStatus,
                                                        LocalDate today,
                                                        SidoCode sidoCode,
                                                        String sigunguCode,
                                                        Pageable pageable) {
        return fetch(basePredicate(null, closedStatus, sidoCode, sigunguCode)
                        .and(upcomingDeadline(today)),
                pageable, deadlineOrder());
    }

    @Override
    public List<Policy> findAllUpcomingDeadlinePoliciesAfter(RecruitmentStatus closedStatus,
                                                             LocalDate today,
                                                             SidoCode sidoCode,
                                                             String sigunguCode,
                                                             LocalDate applyEndDate,
                                                             Long policyId,
                                                             Pageable pageable) {
        return fetch(basePredicate(null, closedStatus, sidoCode, sigunguCode)
                        .and(upcomingDeadline(today))
                        .and(deadlineCursor(applyEndDate, policyId, false)),
                pageable, deadlineOrder());
    }

    @Override
    public List<Policy> findUpcomingDeadlinePolicies(PolicyCategoryType category,
                                                     RecruitmentStatus closedStatus,
                                                     LocalDate today,
                                                     SidoCode sidoCode,
                                                     String sigunguCode,
                                                     Pageable pageable) {
        return fetch(basePredicate(category, closedStatus, sidoCode, sigunguCode)
                        .and(upcomingDeadline(today)),
                pageable, deadlineOrder());
    }

    @Override
    public List<Policy> findUpcomingDeadlinePoliciesAfter(PolicyCategoryType category,
                                                          RecruitmentStatus closedStatus,
                                                          LocalDate today,
                                                          SidoCode sidoCode,
                                                          String sigunguCode,
                                                          LocalDate applyEndDate,
                                                          Long policyId,
                                                          Pageable pageable) {
        return fetch(basePredicate(category, closedStatus, sidoCode, sigunguCode)
                        .and(upcomingDeadline(today))
                        .and(deadlineCursor(applyEndDate, policyId, false)),
                pageable, deadlineOrder());
    }

    @Override
    public List<Policy> findAllLatestPolicies(RecruitmentStatus closedStatus,
                                              SidoCode sidoCode,
                                              String sigunguCode,
                                              Pageable pageable) {
        return fetch(basePredicate(null, closedStatus, sidoCode, sigunguCode),
                pageable, latestOrder());
    }

    @Override
    public List<Policy> findAllLatestPoliciesAfter(RecruitmentStatus closedStatus,
                                                   SidoCode sidoCode,
                                                   String sigunguCode,
                                                   LocalDateTime registeredAt,
                                                   Long policyId,
                                                   Pageable pageable) {
        return fetch(basePredicate(null, closedStatus, sidoCode, sigunguCode)
                        .and(latestCursor(registeredAt, policyId)),
                pageable, latestOrder());
    }

    @Override
    public List<Policy> findLatestPolicies(PolicyCategoryType category,
                                           RecruitmentStatus closedStatus,
                                           SidoCode sidoCode,
                                           String sigunguCode,
                                           Pageable pageable) {
        return fetch(basePredicate(category, closedStatus, sidoCode, sigunguCode),
                pageable, latestOrder());
    }

    @Override
    public List<Policy> findLatestPoliciesAfter(PolicyCategoryType category,
                                                RecruitmentStatus closedStatus,
                                                SidoCode sidoCode,
                                                String sigunguCode,
                                                LocalDateTime registeredAt,
                                                Long policyId,
                                                Pageable pageable) {
        return fetch(basePredicate(category, closedStatus, sidoCode, sigunguCode)
                        .and(latestCursor(registeredAt, policyId)),
                pageable, latestOrder());
    }

    @Override
    public List<Policy> findDeadlinePolicies(PolicyCategoryType category,
                                             RecruitmentStatus closedStatus,
                                             SidoCode sidoCode,
                                             String sigunguCode,
                                             Pageable pageable) {
        return fetch(basePredicate(category, closedStatus, sidoCode, sigunguCode),
                pageable, nullableDeadlineOrder());
    }

    @Override
    public List<Policy> findDeadlinePoliciesAfterDatedCursor(PolicyCategoryType category,
                                                             RecruitmentStatus closedStatus,
                                                             SidoCode sidoCode,
                                                             String sigunguCode,
                                                             LocalDate applyEndDate,
                                                             Long policyId,
                                                             Pageable pageable) {
        return fetch(basePredicate(category, closedStatus, sidoCode, sigunguCode)
                        .and(deadlineCursor(applyEndDate, policyId, true)),
                pageable, nullableDeadlineOrder());
    }

    @Override
    public List<Policy> findDeadlinePoliciesAfterOpenEndedCursor(PolicyCategoryType category,
                                                                 RecruitmentStatus closedStatus,
                                                                 SidoCode sidoCode,
                                                                 String sigunguCode,
                                                                 Long policyId,
                                                                 Pageable pageable) {
        return fetch(basePredicate(category, closedStatus, sidoCode, sigunguCode)
                        .and(policy.applyEndDate.isNull())
                        .and(policy.id.lt(policyId)),
                pageable, nullableDeadlineOrder());
    }

    private BooleanExpression basePredicate(PolicyCategoryType category,
                                            RecruitmentStatus closedStatus,
                                            SidoCode sidoCode,
                                            String sigunguCode) {
        BooleanExpression predicate = policy.recruitmentStatus.ne(closedStatus)
                .and(visibleInRegion(sidoCode, sigunguCode));

        return category == null ? predicate : predicate.and(policy.category.eq(category));
    }

    private BooleanExpression visibleInRegion(SidoCode sidoCode, String sigunguCode) {
        if (sidoCode == null) {
            return policy.national.isTrue();
        }

        BooleanExpression matchingRegion = policyRegion.regionCode.isNull();
        if (sigunguCode != null && !sigunguCode.isBlank()) {
            matchingRegion = matchingRegion.or(policyRegion.regionCode.sigunguCode.eq(sigunguCode));
        }

        return policy.national.isTrue().or(
                JPAExpressions.selectOne()
                        .from(policyRegion)
                        .where(
                                policyRegion.policy.eq(policy),
                                policyRegion.sidoCode.eq(sidoCode),
                                matchingRegion
                        )
                        .exists()
        );
    }

    private BooleanExpression upcomingDeadline(LocalDate today) {
        return policy.applyEndDate.isNotNull()
                .and(policy.applyEndDate.goe(today));
    }

    private BooleanExpression latestCursor(LocalDateTime registeredAt, Long policyId) {
        return policy.registeredAt.lt(registeredAt)
                .or(policy.registeredAt.eq(registeredAt).and(policy.id.lt(policyId)));
    }

    private BooleanExpression deadlineCursor(LocalDate applyEndDate,
                                              Long policyId,
                                              boolean includeOpenEnded) {
        BooleanExpression afterCursor = policy.applyEndDate.gt(applyEndDate)
                .or(policy.applyEndDate.eq(applyEndDate).and(policy.id.lt(policyId)));

        return includeOpenEnded ? afterCursor.or(policy.applyEndDate.isNull()) : afterCursor;
    }

    private long count(BooleanExpression predicate) {
        Long result = queryFactory
                .select(policy.count())
                .from(policy)
                .where(predicate)
                .fetchOne();

        return result == null ? 0L : result;
    }

    private List<Policy> fetch(BooleanExpression predicate,
                               Pageable pageable,
                               OrderSpecifier<?>[] orderSpecifiers) {
        return queryFactory
                .selectFrom(policy)
                .where(predicate)
                .orderBy(orderSpecifiers)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private OrderSpecifier<?>[] latestOrder() {
        return new OrderSpecifier<?>[]{policy.registeredAt.desc(), policy.id.desc()};
    }

    private OrderSpecifier<?>[] deadlineOrder() {
        return new OrderSpecifier<?>[]{policy.applyEndDate.asc(), policy.id.desc()};
    }

    private OrderSpecifier<?>[] nullableDeadlineOrder() {
        return new OrderSpecifier<?>[]{policy.applyEndDate.asc().nullsLast(), policy.id.desc()};
    }
}
