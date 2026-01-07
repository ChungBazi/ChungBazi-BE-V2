package chungbazi.chungbazi_be.domain.community.support;

import chungbazi.chungbazi_be.domain.community.entity.Comment;
import chungbazi.chungbazi_be.domain.community.entity.Post;
import chungbazi.chungbazi_be.domain.community.repository.CommentRepository;
import chungbazi.chungbazi_be.domain.community.repository.PostRepository;
import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.NotFoundHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommunityReader {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_POST));
    }

    public Comment getParentComment(Long parentCommentId) {
        if (parentCommentId == null) {
            return null;
        }

        return commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_COMMENT));
    }
}
