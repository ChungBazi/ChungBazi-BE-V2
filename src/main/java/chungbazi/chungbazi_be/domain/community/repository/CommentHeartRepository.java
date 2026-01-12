package chungbazi.chungbazi_be.domain.community.repository;

import chungbazi.chungbazi_be.domain.community.entity.Comment;
import chungbazi.chungbazi_be.domain.community.entity.CommentHeart;
import chungbazi.chungbazi_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentHeartRepository extends JpaRepository<CommentHeart, Long> {
    boolean existsByUserAndComment(User user, Comment comment);
    boolean existsByUserIdAndCommentId(Long userId, Long commentId);
    Optional<CommentHeart> findByUserAndComment(User user, Comment comment);

    @Query("""
        select ch.comment.id
        from CommentHeart ch
        where ch.user.id = :userId
        and ch.comment.id in :commentIds
    """)
    List<Long> findLikedCommentIds(
            @Param("userId") Long userId,
            @Param("commentIds") List<Long> commentIds
    );
}
