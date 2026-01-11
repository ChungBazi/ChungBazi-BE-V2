package chungbazi.chungbazi_be.domain.community.service;

import chungbazi.chungbazi_be.domain.community.converter.CommunityConverter;
import chungbazi.chungbazi_be.domain.community.dto.CommunityRequestDTO;
import chungbazi.chungbazi_be.domain.community.dto.CommunityResponseDTO;
import chungbazi.chungbazi_be.domain.community.entity.Comment;
import chungbazi.chungbazi_be.domain.community.entity.Post;
import chungbazi.chungbazi_be.domain.community.repository.CommentHeartRepository;
import chungbazi.chungbazi_be.domain.community.repository.CommentRepository;
import chungbazi.chungbazi_be.domain.community.support.CommunityReader;
import chungbazi.chungbazi_be.domain.notification.converter.NotificationConverter;
import chungbazi.chungbazi_be.domain.notification.dto.internal.NotificationData;
import chungbazi.chungbazi_be.domain.notification.service.NotificationService;
import chungbazi.chungbazi_be.domain.report.repository.ReportRepository;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.repository.UserBlockRepository.UserBlockRepository;
import chungbazi.chungbazi_be.domain.user.support.UserHelper;
import chungbazi.chungbazi_be.global.utils.PaginationResult;
import chungbazi.chungbazi_be.global.utils.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentHeartRepository commentHeartRepository;
    private final UserBlockRepository userBlockRepository;
    private final ReportRepository reportRepository;
    private final UserHelper userHelper;
    private final CommunityReader communityReader;
    private final NotificationService notificationService;
    private final RewardService rewardService;

    @Transactional
    public CommunityResponseDTO.UploadAndGetCommentDto uploadComment(CommunityRequestDTO.UploadCommentDto uploadCommentDto) {
        // 게시글 조회
        Post post = communityReader.getPost(uploadCommentDto.getPostId());

        // 부모 댓글이 있는지 확인
        Comment parentComment = communityReader.getParentComment(uploadCommentDto.getParentCommentId());

        // 유저 조회
        User user = userHelper.getAuthenticatedUser();

        Comment comment = CommunityConverter.toEntity(uploadCommentDto.getContent(), user, post, parentComment);
        commentRepository.save(comment);

        handleCommentNotification(user, post, parentComment);
        rewardService.checkRewards();

        return CommunityConverter.toUploadAndGetCommentDto(comment, user.getId(),false,0);
    }

    public CommunityResponseDTO.CommentListDto getComments(Long postId, Long cursor, int size) {
        User user = userHelper.getAuthenticatedUser();

        // 사용자 기준 댓글 조회
        List<Comment> fetched = commentRepository.findCommentsWithFilters(
                postId, cursor, size, user.getId()
        );

        PaginationResult<Comment> pagination = PaginationUtil.paginate(fetched, size);

        // 부모 댓글 기준 대댓글 수 계산
        Map<Long, Integer> replyCountMap = calculateReplyCounts(pagination.getItems());

        List<CommunityResponseDTO.UploadAndGetCommentDto> response =
                buildCommentTree(
                        pagination.getItems(),
                        user.getId(),
                        replyCountMap
                );

        return CommunityConverter.toGetCommentsListDto(
                response,
                pagination.getNextCursor(),
                pagination.isHasNext()
        );
    }

    private void sendCommentNotification(Long postId){
        User user = userHelper.getAuthenticatedUser();

        Post post = communityReader.getPost(postId);

        User author = post.getAuthor();
        String message = user.getName() + "님이 회원님의 게시글에 댓글을 달았습니다.";

        NotificationData request = NotificationConverter.toCommunityEntity(author, message, post.getId());
        notificationService.sendNotification(request);
    }

    private void handleCommentNotification(User user, Post post, Comment parentComment) {
        if (!user.getNotificationSetting().isCommunityAlarm()) {
            return;
        }
        notifyPostAuthor(user, post);
        notifyParentCommentAuthor(user, post, parentComment);
    }

    private void notifyPostAuthor(User user, Post post) {
        if (user.getId().equals(post.getAuthor().getId())) {
            return;
        }
        sendCommentNotification(post.getId());
    }

    private void notifyParentCommentAuthor(User user, Post post, Comment parentComment) {
        if (parentComment == null) {
            return;
        }

        User parentAuthor = parentComment.getAuthor();
        if (parentAuthor.getId().equals(user.getId())) {
            return;
        }

        String message = user.getName() + "님이 회원님의 댓글에 답글을 달았습니다.";

        notificationService.sendNotification(
                NotificationConverter.toCommunityEntity(
                        parentAuthor,
                        message,
                        post.getId()
                )
        );
    }

    private Map<Long, Integer> calculateReplyCounts(List<Comment> comments) {
        Map<Long, Integer> replyCountMap = new HashMap<>();

        for (Comment comment : comments) {
            // 대댓글일 경우
            if (comment.getParentComment() != null) {
                Long parentId = comment.getParentComment().getId();
                replyCountMap.put(parentId, replyCountMap.getOrDefault(parentId, 0) + 1);
            }
        }
        return replyCountMap;
    }

    private List<CommunityResponseDTO.UploadAndGetCommentDto> buildCommentTree(
            List<Comment> comments,
            Long userId,
            Map<Long, Integer> replyCountMap
    ) {
        Map<Long, CommunityResponseDTO.UploadAndGetCommentDto> map = new HashMap<>();
        List<CommunityResponseDTO.UploadAndGetCommentDto> parentComments = new ArrayList<>();

        for (Comment comment : comments) {
            CommunityResponseDTO.UploadAndGetCommentDto dto =
                    CommunityConverter.toUploadAndGetCommentDto(
                            comment,
                            userId,
                            isLikedByUser(comment, userId), // TODO: N + 1 수정
                            replyCountMap.getOrDefault(comment.getId(), 0)
                    );

            map.put(comment.getId(), dto);

            // 부모 댓글일 경우
            if (comment.getParentComment() == null) {
                parentComments.add(dto);
            } else {
                map.get(comment.getParentComment().getId())
                        .getComments()
                        .add(dto);
            }
        }
        return parentComments;
    }

    private boolean isLikedByUser(Comment comment, Long userId) {
        return commentHeartRepository.existsByUserIdAndCommentId(userId, comment.getId());
    }
}
