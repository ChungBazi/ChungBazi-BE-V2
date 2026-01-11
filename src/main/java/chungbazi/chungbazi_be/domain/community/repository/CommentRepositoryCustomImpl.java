package chungbazi.chungbazi_be.domain.community.repository;

import chungbazi.chungbazi_be.domain.community.entity.Comment;
import chungbazi.chungbazi_be.domain.community.entity.ContentStatus;
import chungbazi.chungbazi_be.domain.community.entity.QComment;
import chungbazi.chungbazi_be.domain.report.entity.enums.ReportType;
import chungbazi.chungbazi_be.domain.report.repository.ReportRepository;
import chungbazi.chungbazi_be.domain.user.repository.UserBlockRepository.UserBlockRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final UserBlockRepository userBlockRepository;
    private final ReportRepository reportRepository;

    @Override
    public List<Comment> findCommentsWithFilters(
            Long postId,
            Long cursor,
            int size,
            Long userId
    ) {
        QComment comment = QComment.comment;

        List<Long> blockedUserIds = userBlockRepository.findBlockedUserIdsByBlocker(userId);

        List<Long> reportedCommentIds = reportRepository.findReportedTargetIdsByReporterAndType(
                userId, ReportType.COMMENT
        );

        return queryFactory
                .selectFrom(comment)
                .where(
                        comment.status.eq(ContentStatus.VISIBLE),
                        comment.post.id.eq(postId),
                        afterCursor(cursor),
                        notBlocked(blockedUserIds),
                        notReported(reportedCommentIds)
                )
                .orderBy(comment.id.asc())
                .limit(size + 1)
                .fetch();
    }

    @Override
    public Long countCommentsWithFilters(Long postId, ContentStatus status, List<Long> excludedAuthorIds, List<Long> reportedCommentIds) {
        QComment comment = QComment.comment;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(comment.post.id.eq(postId))
                .and(comment.status.eq(status));

        if (!excludedAuthorIds.isEmpty()) {
            builder.and(comment.author.id.notIn(excludedAuthorIds));
        }
        if (!reportedCommentIds.isEmpty()) {
            builder.and(comment.id.notIn(reportedCommentIds));
        }

        return queryFactory
                .select(comment.count())
                .from(comment)
                .where(builder)
                .fetchOne();
    }

    private BooleanExpression afterCursor(Long cursor) {
        return cursor == null ? null : QComment.comment.id.gt(cursor);
    }


    private BooleanExpression notBlocked(List<Long> ids) {
        return ids.isEmpty() ? null : QComment.comment.author.id.notIn(ids);
    }

    private BooleanExpression notReported(List<Long> ids) {
        return ids.isEmpty() ? null : QComment.comment.id.notIn(ids);
    }
}
