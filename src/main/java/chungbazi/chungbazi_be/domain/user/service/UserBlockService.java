package chungbazi.chungbazi_be.domain.user.service;

import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.repository.UserBlockRepository.UserBlockRepository;
import chungbazi.chungbazi_be.domain.user.support.UserHelper;
import chungbazi.chungbazi_be.domain.user.support.UserReader;
import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import chungbazi.chungbazi_be.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBlockService {

    private final UserHelper userHelper;
    private final UserReader userReader;
    private final UserBlockRepository userBlockRepository;

    @Transactional
    public void blockUser(Long blockedUserId){
        User blocker = userHelper.getAuthenticatedUser();

        User blockedUser = userReader.getUser(blockedUserId);

        if (blockedUser.getId().equals(blocker.getId())){
            throw new GeneralException(ErrorStatus.INVALID_BLOCK);
        }
        userBlockRepository.block(blocker.getId(), blockedUserId);
    }

    @Transactional
    public void unblockUser(Long blockedUserId){
        User blocker = userHelper.getAuthenticatedUser();
        userBlockRepository.unblock(blocker.getId(), blockedUserId);
    }

    public boolean isUserBlocked(Long targetUserId){
        User currentUser = userHelper.getAuthenticatedUser();
        return userBlockRepository.existsBlockBetweenUsers(currentUser.getId(), targetUserId);
    }
}
