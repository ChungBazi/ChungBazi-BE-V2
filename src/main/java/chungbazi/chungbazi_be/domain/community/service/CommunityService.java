package chungbazi.chungbazi_be.domain.community.service;

import chungbazi.chungbazi_be.domain.community.converter.CommunityConverter;
import chungbazi.chungbazi_be.domain.community.dto.CommunityRequestDTO;
import chungbazi.chungbazi_be.domain.community.dto.CommunityResponseDTO;
import chungbazi.chungbazi_be.domain.community.entity.*;
import chungbazi.chungbazi_be.domain.community.repository.CommentHeartRepository;
import chungbazi.chungbazi_be.domain.community.repository.CommentRepository;
import chungbazi.chungbazi_be.domain.community.repository.HeartRepository;
import chungbazi.chungbazi_be.domain.community.repository.PostRepository;
import chungbazi.chungbazi_be.domain.notification.dto.NotificationRequest;
import chungbazi.chungbazi_be.domain.notification.entity.enums.NotificationType;
import chungbazi.chungbazi_be.domain.notification.service.NotificationService;
import chungbazi.chungbazi_be.domain.policy.entity.Category;
import chungbazi.chungbazi_be.domain.report.entity.enums.ReportType;
import chungbazi.chungbazi_be.domain.report.repository.ReportRepository;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.repository.UserBlockRepository.UserBlockRepository;
import chungbazi.chungbazi_be.domain.user.utils.UserHelper;
import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.BadRequestHandler;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.NotFoundHandler;
import chungbazi.chungbazi_be.global.s3.S3Manager;
import chungbazi.chungbazi_be.global.utils.PaginationResult;
import chungbazi.chungbazi_be.global.utils.PaginationUtil;
import chungbazi.chungbazi_be.global.utils.PopularSearch;
import java.time.LocalDateTime;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommunityService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final S3Manager s3Manager;
    private final RewardService rewardService;
    private final NotificationService notificationService;
    private final UserHelper userHelper;
    private final HeartRepository heartRepository;
    private final PopularSearch popularSearch;
    private final ReportRepository reportRepository;
    private final UserBlockRepository userBlockRepository;
    private final CommentHeartRepository commentHeartRepository;

    public CommunityResponseDTO.TotalPostListDto getPosts(Category category, Long cursor, int size) {
        Pageable pageable = PageRequest.of(0, size + 1);
        List<Post> posts;

        User user = userHelper.getAuthenticatedUser();
        List<Long> blockedUserIds = userBlockRepository.findBlockedUserIdsByBlocker(user.getId());
        List<Long> reportedPostIds = reportRepository.findReportedTargetIdsByReporterAndType(user.getId(), ReportType.POST);

        if (blockedUserIds.isEmpty()) {
            blockedUserIds = Arrays.asList(-1L);
        }
        if (reportedPostIds.isEmpty()) {
            reportedPostIds = Arrays.asList(-1L);
        }

        if (category == null || category.toString().isEmpty()){ // 전체 게시글 조회
            posts = (cursor == 0)
                    ? postRepository.findByStatusAndAuthorIdNotInAndIdNotInOrderByIdDesc(ContentStatus.VISIBLE,blockedUserIds,reportedPostIds,pageable).getContent()
                    : postRepository.findByStatusAndAuthorIdNotInAndIdNotInAndIdLessThanOrderByIdDesc(ContentStatus.VISIBLE,blockedUserIds,reportedPostIds,cursor, pageable).getContent();
        } else { // 카테고리별 게시글 조회
            posts = (cursor == 0)
                    ? postRepository.findByCategoryAndStatusAndAuthorIdNotInAndIdNotInOrderByIdDesc(category, ContentStatus.VISIBLE, blockedUserIds,reportedPostIds,pageable).getContent()
                    : postRepository.findByCategoryAndStatusAndAuthorIdNotInAndIdNotInAndIdLessThanOrderByIdDesc(category, ContentStatus.VISIBLE, blockedUserIds,reportedPostIds,cursor, pageable).getContent();
        }

        PaginationResult<Post> paginationResult = PaginationUtil.paginate(posts, size);
        posts = paginationResult.getItems();

        List<CommunityResponseDTO.PostListDto> postList = CommunityConverter.toPostListDto(posts, commentRepository,heartRepository,blockedUserIds,reportedPostIds,user);
        Long totalPostCount = postRepository.countPostByCategoryAndStatusAndAuthorIdNotInAndIdNotIn(category,ContentStatus.VISIBLE,blockedUserIds,reportedPostIds);

        return CommunityConverter.toTotalPostListDto(
                totalPostCount,
                postList,
                paginationResult.getNextCursor(),
                paginationResult.isHasNext());
    }
    public CommunityResponseDTO.UploadAndGetPostDto uploadPost(CommunityRequestDTO.UploadPostDto uploadPostDto, List<MultipartFile> imageList){
        User user = userHelper.getAuthenticatedUser();

        // 파일 수 검증
        if (imageList != null && imageList.size() > 10) {
            throw new BadRequestHandler(ErrorStatus.FILE_COUNT_EXCEEDED);
        }

        // 파일 업로드
        List<String> uploadedUrls = (imageList != null && !imageList.isEmpty())
                ? s3Manager.uploadMultipleFiles(imageList, "post-images") : new ArrayList<>();

        Post post = Post.builder()
                .title(uploadPostDto.getTitle())
                .content(uploadPostDto.getContent())
                .category(uploadPostDto.getCategory())
                .author(user)
                .views(0)
                .postLikes(0)
                .imageUrls(uploadedUrls)
                .anonymous(uploadPostDto.isAnonymous())
                .status(ContentStatus.VISIBLE)
                .build();
        postRepository.save(post);

        long commentCount = 0L;

        rewardService.checkRewards();

        return CommunityConverter.toUploadAndGetPostDto(post, commentCount, true,false);
    }

    public CommunityResponseDTO.UploadAndGetPostDto getPost(Long postId) {
        Post post = postRepository.getReferenceById(postId);
        // 자신의 조회는 조회수 증가 제외
        User user = userHelper.getAuthenticatedUser();
        List<Long> blockedUserIds = userBlockRepository.findBlockedUserIdsByBlocker(user.getId());
        List<Long> reportedCommentIds = reportRepository.findReportedTargetIdsByReporterAndType(user.getId(), ReportType.COMMENT);

        if (blockedUserIds.isEmpty()) {
            blockedUserIds = Arrays.asList(-1L);
        }
        if (reportedCommentIds.isEmpty()) {
            reportedCommentIds = Arrays.asList(-1L);
        }

        if(!post.getAuthor().getId().equals(user.getId())){
            post.incrementViews(); // 조회수 증가
        }
        Long commentCount = commentRepository.countCommentsWithFilters(postId,ContentStatus.VISIBLE,blockedUserIds,reportedCommentIds);

        boolean isMine = post.getAuthor().equals(user);
        boolean isLikedByUser = heartRepository.existsByUserAndPost(user, post);

        return CommunityConverter.toUploadAndGetPostDto(post, commentCount, isMine,isLikedByUser);
    }

    public CommunityResponseDTO.UploadAndGetCommentDto uploadComment(CommunityRequestDTO.UploadCommentDto uploadCommentDto) {
        // 게시글 조회
        Post post = postRepository.findById(uploadCommentDto.getPostId())
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_POST));

        //부모 댓글이 있는지 확인
        Comment parentComment = null;
        if (uploadCommentDto.getParentCommentId() !=null){
            parentComment = commentRepository.findById(uploadCommentDto.getParentCommentId())
                    .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_COMMENT));
        }

        // 유저 조회
        User user = userHelper.getAuthenticatedUser();
        Comment comment = Comment.builder().
                content(uploadCommentDto.getContent())
                .author(user)
                .post(post)
                .status(ContentStatus.VISIBLE)
                .parentComment(parentComment)
                .build();

        commentRepository.save(comment);

        if(user.getNotificationSetting().isCommunityAlarm() && !user.getId().equals(post.getAuthor().getId())){
            sendCommunityNotification(post.getId());
        }
        if (parentComment != null) {
            User parentAuthor = parentComment.getAuthor();
            // 부모 댓글 작성자가 댓글 작성자 자신이 아닐 때만 알림
            if (user.getNotificationSetting().isCommunityAlarm()
                    && !parentAuthor.getId().equals(user.getId())) {
                String message = user.getName() + "님이 회원님의 댓글에 답글을 달았습니다.";
                NotificationRequest request = NotificationRequest.builder()
                        .user(user)
                        .type(NotificationType.POST)
                        .targetId(post.getId())
                        .build();

                notificationService.sendNotification(request);
            }
        }
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

    public void likePost(Long postId){
        User user = userHelper.getAuthenticatedUser();
        Post post = postRepository.getReferenceById(postId);

        //이미 좋아요한 경우
        if(heartRepository.existsByUserAndPost(user, post)) {
            throw new BadRequestHandler(ErrorStatus.ALREADY_LIKED);
        }

        Heart heart = Heart.builder().user(user).post(post).build();
        heartRepository.save(heart);

        post.incrementLike();
        postRepository.save(post);

        if (user.getNotificationSetting().isCommunityAlarm() && !user.getId().equals(post.getAuthor().getId())){
            sendPostLikeNotification(postId);
        }
    }

    public void unlikePost(Long postId){
        User user = userHelper.getAuthenticatedUser();
        Post post = postRepository.getReferenceById(postId);

        Heart heart = heartRepository.findByUserAndPost(user,post)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_POST_LIKE));
        heartRepository.delete(heart);

        post.decrementLike();
        postRepository.save(post);
    }

    public void sendCommunityNotification(Long postId){
        User user = userHelper.getAuthenticatedUser();

        Post post = postRepository.getReferenceById(postId);

        User author=post.getAuthor();
        String message=user.getName()+"님이 회원님의 게시글에 댓글을 달았습니다.";

        NotificationRequest request = NotificationRequest.builder()
                .user(author)
                .type(NotificationType.COMMENT)
                .message(message)
                .targetId(post.getId())
                .build();

        notificationService.sendNotification(request);
    }

    public void sendPostLikeNotification(Long postId){
        User user = userHelper.getAuthenticatedUser();

        Post post = postRepository.getReferenceById(postId);

        User author=post.getAuthor();
        String message = user.getName()+"님이 회원님의 게시글에 좋아요를 누르셨습니다.";

        NotificationRequest request = NotificationRequest.builder()
                .user(author)
                .type(NotificationType.POST)
                .message(message)
                .targetId(postId)
                .build();

        notificationService.sendNotification(request);
    }

    public void sendCommentLikeNotification(Long commentId){
        User user = userHelper.getAuthenticatedUser();

        Comment comment = commentRepository.getReferenceById(commentId);

        Post post = comment.getPost();
        User author=comment.getAuthor();
        String message = user.getName()+"님이 회원님의 댓글에 좋아요를 누르셨습니다.";

        NotificationRequest request = NotificationRequest.builder()
                .user(author)
                .type(NotificationType.COMMENT)
                .message(message)
                .targetId(post.getId())
                .build();

        notificationService.sendNotification(request);
    }

    public CommunityResponseDTO.TotalPostListDto getSearchPost(String query, String filter, String period, Long cursor, int size) {
        Pageable pageable = PageRequest.of(0, size + 1);
        List<Post> posts;
        LocalDateTime startDate = getStartDateByPeriod(period);

        User user = userHelper.getAuthenticatedUser();
        List<Long> blockedUserIds = userBlockRepository.findBlockedUserIdsByBlocker(user.getId());
        List<Long> reportedPostIds = reportRepository.findReportedTargetIdsByReporterAndType(user.getId(), ReportType.POST);

        if (blockedUserIds.isEmpty()) {
            blockedUserIds = Arrays.asList(-1L);
        }
        if (reportedPostIds.isEmpty()) {
            reportedPostIds = Arrays.asList(-1L);
        }

        if(!filter.equals("title") && !filter.equals("content")){
            throw new BadRequestHandler(ErrorStatus._BAD_REQUEST);
        }
        String searchField = filter.equals("title") ? "title" : "content";

        if (searchField.equals("title")) { // 제목으로 검색
            posts = (cursor == 0)
                    ? postRepository.findByStatusAndAuthorIdNotInAndIdNotInAndTitleContainingAndCreatedAtAfterOrderByIdDesc(ContentStatus.VISIBLE,blockedUserIds,reportedPostIds,query, startDate, pageable).getContent()
                    : postRepository.findByStatusAndAuthorIdNotInAndIdNotInAndTitleContainingAndCreatedAtAfterAndIdLessThanOrderByIdDesc(ContentStatus.VISIBLE,blockedUserIds,reportedPostIds,query, startDate, cursor, pageable).getContent();
        } else { // 내용으로 검색
            posts = (cursor == 0)
                    ? postRepository.findByStatusAndAuthorIdNotInAndIdNotInAndContentContainingAndCreatedAtAfterOrderByIdDesc(ContentStatus.VISIBLE,blockedUserIds,reportedPostIds,query, startDate, pageable).getContent()
                    : postRepository.findByStatusAndAuthorIdNotInAndIdNotInAndContentContainingAndCreatedAtAfterAndIdLessThanOrderByIdDesc(ContentStatus.VISIBLE,blockedUserIds,reportedPostIds,query, startDate, cursor, pageable).getContent();
        }

        popularSearch.updatePopularSearch(query, "community");

        PaginationResult<Post> paginationResult = PaginationUtil.paginate(posts, size);
        posts = paginationResult.getItems();

        List<CommunityResponseDTO.PostListDto> postList = CommunityConverter.toPostListDto(posts, commentRepository,heartRepository,blockedUserIds,reportedPostIds,user);

        return CommunityConverter.toTotalPostListDto(
                null,
                postList,
                paginationResult.getNextCursor(),
                paginationResult.isHasNext());
    }
    private LocalDateTime getStartDateByPeriod(String period) {
        switch (period) {
            case "1d": return LocalDateTime.now().minusDays(1);
            case "7d": return LocalDateTime.now().minusDays(7);
            case "1m": return LocalDateTime.now().minusMonths(1);
            case "3m": return LocalDateTime.now().minusMonths(3);
            case "6m": return LocalDateTime.now().minusMonths(6);
            case "1y": return LocalDateTime.now().minusYears(1);
            default: return LocalDateTime.of(2025, 1, 1, 0, 0); // 전체 조회
        }
    }

    public void deletePost(Long postId){
        User user = userHelper.getAuthenticatedUser();

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_POST));

        if (!post.getAuthor().equals(user)) {
            throw new BadRequestHandler(ErrorStatus.UNABLE_TO_DELETE_POST);
        }

        postRepository.delete(post);
    }

    public void deleteComment(Long commentId){
        User user = userHelper.getAuthenticatedUser();

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_COMMENT));

        if (!comment.getAuthor().equals(user)) {
            throw new BadRequestHandler(ErrorStatus.UNABLE_TO_DELETE_COMMENT);
        }

        comment.markAsDeleted();
        commentRepository.save(comment);
    }

    public void likeComment(Long commentId) {
        User user = userHelper.getAuthenticatedUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_COMMENT));

        //이미 좋아요한 경우
        if(commentHeartRepository.existsByUserAndComment(user, comment)) {
            throw new BadRequestHandler(ErrorStatus.ALREADY_LIKED);
        }
        CommentHeart commentHeart = CommentHeart.builder()
                .user(user)
                .comment(comment)
                .build();
        commentHeartRepository.save(commentHeart);

        comment.incrementLike();
        commentRepository.save(comment);

        if (user.getNotificationSetting().isCommunityAlarm() && !user.getId().equals(comment.getAuthor().getId())){
            sendCommentLikeNotification(commentId);
        }
    }

    public void unlikeComment(Long commentId){
        User user = userHelper.getAuthenticatedUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_COMMENT));

        CommentHeart commentHeart = commentHeartRepository.findByUserAndComment(user,comment)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_COMMENT_LIKE));
        commentHeartRepository.delete(commentHeart);

        comment.decrementLike();
        commentRepository.save(comment);

    }
}
