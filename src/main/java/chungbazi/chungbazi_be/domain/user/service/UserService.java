package chungbazi.chungbazi_be.domain.user.service;


import chungbazi.chungbazi_be.domain.auth.jwt.SecurityUtils;
import chungbazi.chungbazi_be.domain.community.entity.ContentStatus;
import chungbazi.chungbazi_be.domain.community.repository.CommentRepository;
import chungbazi.chungbazi_be.domain.community.repository.PostRepository;
import chungbazi.chungbazi_be.domain.user.converter.UserConverter;
import chungbazi.chungbazi_be.domain.user.dto.UserRequestDTO;
import chungbazi.chungbazi_be.domain.user.dto.UserResponseDTO;
import chungbazi.chungbazi_be.domain.user.entity.Addition;
import chungbazi.chungbazi_be.domain.user.entity.Interest;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.entity.mapping.UserAddition;
import chungbazi.chungbazi_be.domain.user.entity.mapping.UserInterest;
import chungbazi.chungbazi_be.domain.user.repository.*;
import chungbazi.chungbazi_be.domain.user.utils.UserHelper;
import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.BadRequestHandler;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.NotFoundHandler;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AdditionRepository additionRepository;
    private final UserAdditionRepository userAdditionRepository;
    private final InterestRepository interestRepository;
    private final UserInterestRepository userInterestRepository;
    private final UserHelper userHelper;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public UserResponseDTO.ProfileDto getProfile() {
        User user = userHelper.getAuthenticatedUser();
        return UserConverter.toProfileDto(user);
    }
    public UserResponseDTO.CharacterImgDto getCharacterImg() {
        User user = userHelper.getAuthenticatedUser();
        return UserConverter.toCharacterImgDto(user);
    }
    public UserResponseDTO.RewardDto getReward() {
        User user = userHelper.getAuthenticatedUser();
        int rewardLevel = user.getReward().getLevel();
        int postCount = postRepository.countPostByAuthorId(user.getId());
        int commentCount = commentRepository.countCommentByAuthorIdAndStatus(user.getId(), ContentStatus.VISIBLE);
        return UserConverter.toRewardDto(rewardLevel, postCount, commentCount);
    }

    public void updateProfile(UserRequestDTO.ProfileUpdateDto profileUpdateDto) {
        User user = userHelper.getAuthenticatedUser();

        // 유저 레벨보다 높은 캐릭터 선택 시 에러 핸들링
        if (profileUpdateDto.getCharacterImg().getLevel() > user.getReward().getLevel()) {
            throw new BadRequestHandler(ErrorStatus.INVALID_CHARACTER);
        }

        // 닉네임 및 이미지 변경 여부 확인
        if (!user.getName().equals(profileUpdateDto.getName())) { // 기존 닉네임과 입력받은 닉네임이 다를 경우
            boolean isDuplicateName = userRepository.existsByName(profileUpdateDto.getName());
            if (isDuplicateName) {
                throw new BadRequestHandler(ErrorStatus.INVALID_NICKNAME);
            }
            user.setName(profileUpdateDto.getName());
        }

        if (!user.getCharacterImg().equals(profileUpdateDto.getCharacterImg())) { // 기존 이미지와 입력받은 이미지가 다를 경우
            user.setCharacterImg(profileUpdateDto.getCharacterImg());
        }
        userRepository.save(user);
    }

    public void registerUserInfo(UserRequestDTO.RegisterDto registerDto) {
        Long userId = SecurityUtils.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_USER));

        user.updateRegion(registerDto.getRegion());
        user.updateEmployment(registerDto.getEmployment());
        user.updateIncome(registerDto.getIncome());
        user.updateEducation(registerDto.getEducation());
        updateInterests(user, registerDto.getInterests());
        updateAdditions(user, registerDto.getAdditionInfo());
        user.setSurveyStatus(true);
    }

    public void updateUserInfo(UserRequestDTO.UpdateDto updateDto) {
        Long userId = SecurityUtils.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_USER));
        Optional.ofNullable(updateDto.getRegion()).ifPresent(user::updateRegion);
        Optional.ofNullable(updateDto.getEmployment()).ifPresent(user::updateEmployment);
        Optional.ofNullable(updateDto.getIncome()).ifPresent(user::updateIncome);
        Optional.ofNullable(updateDto.getEducation()).ifPresent(user::updateEducation);
        Optional.ofNullable(updateDto.getInterests()).ifPresent(interests -> updateInterests(user, interests));
        Optional.ofNullable(updateDto.getAdditionInfo()).ifPresent(additions -> updateAdditions(user, additions));
    }

    private void updateAdditions(User user, List<String> additionalInfo) {
        userAdditionRepository.deleteByUser(user);
        for (String additionName : additionalInfo) {
            Addition addition = additionRepository.findByName(additionName)
                    .orElseGet(() -> additionRepository.save(Addition.from(additionName)));
            userAdditionRepository.save(UserAddition.builder().user(user).addition(addition).build());
        }
    }

    private void updateInterests(User user, List<String> interests) {
        userInterestRepository.deleteByUser(user);
        for (String interestName : interests) {
            Interest interest = interestRepository.findByName(interestName)
                    .orElseGet(() -> interestRepository.save(Interest.from(interestName)));
            userInterestRepository.save(UserInterest.builder().user(user).interest(interest).build());
        }
    }
}