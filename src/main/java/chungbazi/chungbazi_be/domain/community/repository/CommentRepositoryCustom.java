package chungbazi.chungbazi_be.domain.community.repository;

import chungbazi.chungbazi_be.domain.community.entity.Comment;
import chungbazi.chungbazi_be.domain.community.entity.ContentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepositoryCustom {

    List<Comment> findCommentsWithFilters(
            Long postId,
            Long cursor,
            int size,
            Long userId
    );

    Long countCommentsWithFilters(Long postId,
                                  ContentStatus status,
                                  List<Long> excludedAuthorIds,
                                  List<Long> reportedCommentIds);
}
