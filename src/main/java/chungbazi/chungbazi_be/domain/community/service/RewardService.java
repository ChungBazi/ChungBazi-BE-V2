package chungbazi.chungbazi_be.domain.community.service;

import chungbazi.chungbazi_be.domain.character.dto.CharacterResponseDTO.NextLevelInfo;
import chungbazi.chungbazi_be.domain.character.entity.Character;
import chungbazi.chungbazi_be.domain.community.entity.ContentStatus;
import chungbazi.chungbazi_be.domain.community.repository.CommentRepository;
import chungbazi.chungbazi_be.domain.community.repository.PostRepository;
import chungbazi.chungbazi_be.domain.notification.dto.internal.NotificationData;
import chungbazi.chungbazi_be.domain.notification.entity.enums.NotificationType;
import chungbazi.chungbazi_be.domain.notification.service.NotificationService;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.entity.enums.RewardLevel;
import chungbazi.chungbazi_be.domain.user.repository.UserRepository;
import chungbazi.chungbazi_be.domain.user.utils.UserHelper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RewardService {
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserHelper userHelper;

    public void checkRewards() {
        User user = userHelper.getAuthenticatedUser();

        int currentReward = user.getReward().getLevel();

        if (currentReward < RewardLevel.LEVEL_10.getLevel()) {
            RewardLevel nextRewardLevel = RewardLevel.getNextRewardLevel(currentReward);
            if (nextRewardLevel != null) {
                int requiredCount = nextRewardLevel.getThreashold();
                int postCount = postRepository.countPostByAuthorId(user.getId());
                int commentCount = commentRepository.countCommentByAuthorIdAndStatus(user.getId(), ContentStatus.VISIBLE);

                if (postCount >= requiredCount && commentCount >= requiredCount) {
                    user.updateRewardLevel(nextRewardLevel);

                    if (user.getNotificationSetting().isRewardAlarm()){
                        sendRewardNotification(nextRewardLevel.getLevel());
                    }
                }
            }
        }
        userRepository.save(user);
    }

    private void sendRewardNotification(int rewardLevel) {
        User user=userHelper.getAuthenticatedUser();
        String message = rewardLevel + "단계에 달성하여 캐릭터가 지급되었습니다.";

        NotificationData request = NotificationData.builder()
                .user(user)
                .type(NotificationType.REWARD)
                .message(message)
                .build();

        notificationService.sendNotification(request);
    }

    public NextLevelInfo calNextLevelInfo(User user, Character character){
        int currentRewardLevel = character.getRewardLevel().getLevel();

        if(currentRewardLevel >= RewardLevel.LEVEL_10.getLevel()){
            return new NextLevelInfo(null, 0, 0);
        }

        RewardLevel nextRewardLevel = RewardLevel.getNextRewardLevel(currentRewardLevel);

        if(nextRewardLevel != null) {
            int requiredPosts = Math.max(0,
                    nextRewardLevel.getThreashold() - postRepository.countPostByAuthorId(user.getId()));
            int requiredComments = Math.max(0,
                    nextRewardLevel.getThreashold() - commentRepository.countCommentByAuthorIdAndStatus(user.getId(),ContentStatus.VISIBLE));

            return new NextLevelInfo(nextRewardLevel.name(), requiredPosts, requiredComments);
        }

        return new NextLevelInfo(null, 0, 0);

    }
}
