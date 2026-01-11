package chungbazi.chungbazi_be.domain.community.repository;

import chungbazi.chungbazi_be.domain.community.entity.Comment;
import chungbazi.chungbazi_be.domain.community.entity.CommentHeart;
import chungbazi.chungbazi_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentHeartRepository extends JpaRepository<CommentHeart, Long> {
    boolean existsByUserAndComment(User user, Comment comment);
    boolean existsByUserIdAndCommentId(Long userId, Long commentId);
    Optional<CommentHeart> findByUserAndComment(User user, Comment comment);
}
