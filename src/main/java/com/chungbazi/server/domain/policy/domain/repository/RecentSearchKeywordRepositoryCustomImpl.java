package com.chungbazi.server.domain.policy.domain.repository;

import com.chungbazi.server.domain.policy.domain.entity.RecentSearchKeyword;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.chungbazi.server.domain.policy.domain.entity.QRecentSearchKeyword.recentSearchKeyword;

@Repository
@RequiredArgsConstructor
public class RecentSearchKeywordRepositoryCustomImpl implements RecentSearchKeywordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<RecentSearchKeyword> findRecentSearchKeywords(
            Long userId,
            LocalDateTime lastSearchedAt,
            Long keywordId,
            Pageable pageable
    ) {
        return queryFactory
                .selectFrom(recentSearchKeyword)
                .where(
                        recentSearchKeyword.userId.eq(userId),
                        cursorPredicate(lastSearchedAt, keywordId)
                )
                .orderBy(
                        recentSearchKeyword.lastSearchedAt.desc(),
                        recentSearchKeyword.id.desc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression cursorPredicate(
            LocalDateTime lastSearchedAt,
            Long keywordId
    ) {
        if (lastSearchedAt == null || keywordId == null) {
            return null;
        }

        return recentSearchKeyword.lastSearchedAt.lt(lastSearchedAt)
                .or(
                        recentSearchKeyword.lastSearchedAt.eq(lastSearchedAt)
                                .and(recentSearchKeyword.id.lt(keywordId))
                );
    }
}
