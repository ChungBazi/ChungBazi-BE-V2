package chungbazi.chungbazi_be.domain.report.service;

import chungbazi.chungbazi_be.domain.community.entity.Comment;
import chungbazi.chungbazi_be.domain.community.entity.Post;
import chungbazi.chungbazi_be.domain.community.repository.CommentRepository;
import chungbazi.chungbazi_be.domain.community.repository.PostRepository;
import chungbazi.chungbazi_be.domain.report.dto.ReportRequest;
import chungbazi.chungbazi_be.domain.report.entity.Report;
import chungbazi.chungbazi_be.domain.report.entity.enums.ReportReason;
import chungbazi.chungbazi_be.domain.report.entity.enums.ReportType;
import chungbazi.chungbazi_be.domain.report.repository.ReportRepository;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.repository.UserRepository;
import chungbazi.chungbazi_be.domain.user.support.UserHelper;
import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import chungbazi.chungbazi_be.global.apiPayload.exception.GeneralException;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.NotFoundHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static chungbazi.chungbazi_be.domain.report.entity.enums.ReportType.*;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final UserHelper userHelper;

    // 신고 임계값 설정
    private static final int POST_REPORT_THRESHOLD = 3;
    private static final int USER_REPORT_THRESHOLD = 5;
    private static final int COMMENT_REPORT_THRESHOLD = 3;

    public void reportPost(Long postId, ReportRequest.ReportRequestDto dto) {
        User reporter  = userHelper.getAuthenticatedUser();
        report(ReportType.POST, postId, reporter, dto.getReason(), dto.getDescription());
    }

    public void reportUser(Long userId, ReportRequest.ReportRequestDto dto) {
        User reporter  = userHelper.getAuthenticatedUser();
        report(ReportType.USER, userId, reporter, dto.getReason(), dto.getDescription());
    }

    public void reportComment(Long commentId, ReportRequest.ReportRequestDto dto) {
        User reporter  = userHelper.getAuthenticatedUser();
        report(COMMENT, commentId, reporter, dto.getReason(), dto.getDescription());
    }

    public void report(ReportType reportType,Long targetId, User reporter, ReportReason reason, String description) {

        if (reason == ReportReason.OTHER && (description == null || description.trim().isEmpty())) {
            throw new GeneralException(ErrorStatus.DESCRIPTION_REQUIRED);
        }

        if(reportRepository.existsByReporterAndReportTypeAndTargetId(reporter, reportType, targetId)) {
            throw new GeneralException(ErrorStatus.ALREADY_REPORT);
        }

        switch (reportType) {
            case POST :
                Post post = postRepository.findById(targetId)
                        .orElseThrow(()-> new NotFoundHandler(ErrorStatus.NOT_FOUND_POST));

                if (post.getAuthor().equals(reporter)) {
                    throw new GeneralException(ErrorStatus.UNABLE_REPORT_MYSELF);
                }

                post.increaseReportCount();

                if(post.getReportCount() >= POST_REPORT_THRESHOLD) {
                    post.autoHide();
                }

                post.getAuthor().increaseReportCount();
                checkAndBlacklistUser(post.getAuthor());

                postRepository.save(post);
                break;

            case COMMENT :
                Comment comment = commentRepository.findById(targetId)
                        .orElseThrow(()-> new NotFoundHandler(ErrorStatus.NOT_FOUND_COMMENT));

                if (comment.getAuthor().equals(reporter)) {
                    throw new GeneralException(ErrorStatus.UNABLE_REPORT_MYSELF);
                }

                comment.increaseReportCount();
                if (comment.getReportCount() >= COMMENT_REPORT_THRESHOLD) {
                    comment.autoHide();

                }

                comment.getAuthor().increaseReportCount();
                checkAndBlacklistUser(comment.getAuthor());

                commentRepository.save(comment);
                break;

            case USER:
                User user = userRepository.findById(targetId)
                        .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_USER));

                if (user.equals(reporter)) {
                    throw new GeneralException(ErrorStatus.UNABLE_REPORT_MYSELF);
                }
                user.increaseReportCount();
                checkAndBlacklistUser(user);

                userRepository.save(user);

                break;
        }

        Report report = Report.builder()
                .reporter(reporter)
                .reportType(reportType)
                .reportReason(reason)
                .description(description)
                .targetId(targetId)
                .build();

        reportRepository.save(report);
    }



    private void checkAndBlacklistUser(User user) {
        if (!user.isBlacklisted() && user.getReportCount() >= USER_REPORT_THRESHOLD) {
            user.blacklist();
        }
    }

    @Transactional
    public void reportDelete(Long id, ReportType reportType) {
        Report report = null;
        switch (reportType){
            case POST :
                Post post = postRepository.findById(id)
                        .orElseThrow(()-> new NotFoundHandler(ErrorStatus.NOT_FOUND_POST));

                post.decreaseReportCount();

                report = reportRepository.findByTargetIdAndReportType(post.getId(), POST);
                reportRepository.delete(report);
                break;

            case COMMENT :
                Comment comment = commentRepository.findById(id)
                        .orElseThrow(()-> new NotFoundHandler(ErrorStatus.NOT_FOUND_COMMENT));

                comment.decreaseReportCount();

                report = reportRepository.findByTargetIdAndReportType(comment.getId(), COMMENT);
                reportRepository.delete(report);
                break;

            case USER:
                User user = userRepository.findById(id)
                        .orElseThrow(()-> new NotFoundHandler(ErrorStatus.NOT_FOUND_USER));

                user.decreaseReportCount();

                report = reportRepository.findByTargetIdAndReportType(user.getId(), USER);
                reportRepository.delete(report);
                break;

        }
    }
}
