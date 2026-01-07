package chungbazi.chungbazi_be.domain.community.service;

import chungbazi.chungbazi_be.domain.community.converter.CommunityConverter;
import chungbazi.chungbazi_be.domain.community.dto.CommunityRequestDTO;
import chungbazi.chungbazi_be.domain.community.dto.CommunityResponseDTO;
import chungbazi.chungbazi_be.domain.community.entity.Comment;
import chungbazi.chungbazi_be.domain.community.entity.ContentStatus;
import chungbazi.chungbazi_be.domain.community.entity.Post;
import chungbazi.chungbazi_be.domain.community.repository.CommentHeartRepository;
import chungbazi.chungbazi_be.domain.community.repository.CommentRepository;
import chungbazi.chungbazi_be.domain.community.support.CommunityReader;
import chungbazi.chungbazi_be.domain.notification.converter.NotificationConverter;
import chungbazi.chungbazi_be.domain.notification.dto.internal.NotificationData;
import chungbazi.chungbazi_be.domain.notification.service.NotificationService;
import chungbazi.chungbazi_be.domain.report.entity.enums.ReportType;
import chungbazi.chungbazi_be.domain.report.repository.ReportRepository;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.repository.UserBlockRepository.UserBlockRepository;
import chungbazi.chungbazi_be.domain.user.support.UserHelper;
import chungbazi.chungbazi_be.global.utils.PaginationResult;
import chungbazi.chungbazi_be.global.utils.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public CommunityResponseDTO.CommentListDto getComments(Long postId, Long cursor, int size){
        Pageable pageable = PageRequest.of(0, size + 1);

        User user = userHelper.getAuthenticatedUser();
        List<Long> blockedUserIds = userBlockRepository.findBlockedUserIdsByBlocker(user.getId());
        List<Long> reportedCommentIds = reportRepository.findReportedTargetIdsByReporterAndType(user.getId(), ReportType.COMMENT);

        if (blockedUserIds.isEmpty()) {
            blockedUserIds = Arrays.asList(-1L);
        }
        if (reportedCommentIds.isEmpty()) {
            reportedCommentIds = Arrays.asList(-1L);
        }

        List<Comment> comments;

        comments = commentRepository.findCommentsWithFilters(ContentStatus.VISIBLE,blockedUserIds,reportedCommentIds,postId, cursor, pageable).getContent();

        PaginationResult<Comment> paginationResult = PaginationUtil.paginate(comments, size);
        comments = paginationResult.getItems();

        List<CommunityResponseDTO.UploadAndGetCommentDto> responseList = new ArrayList<>();
        Map<Long, CommunityResponseDTO.UploadAndGetCommentDto> responseMap = new HashMap<>();

        //각 부모 댓글의 대댓글 수 계산
        Map<Long, Integer> replyCountMap = new HashMap<>();
        comments.forEach(comment -> {
            if (comment.getParentComment() != null) {
                Long parentId = comment.getParentComment().getId();
                replyCountMap.put(parentId, replyCountMap.getOrDefault(parentId, 0) + 1);
            }
        });

        comments.forEach(comment -> {
            boolean isLikedByUser = commentHeartRepository.existsByUserAndComment(user, comment);
            int replyCount = replyCountMap.getOrDefault(comment.getId(), 0);
            CommunityResponseDTO.UploadAndGetCommentDto dto = CommunityConverter.toUploadAndGetCommentDto(comment, user.getId(), isLikedByUser, replyCount);
            responseMap.put(comment.getId(), dto);

            Optional.ofNullable(comment.getParentComment())
                    .map(parent -> responseMap.get(parent.getId()))
                    .ifPresent(parent -> parent.getComments().add(dto));

            if (comment.getParentComment() ==null){
                responseList.add(dto);
            }
        });

        return CommunityConverter.toGetCommentsListDto(
                responseList,
                paginationResult.getNextCursor(),
                paginationResult.isHasNext());
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
}
