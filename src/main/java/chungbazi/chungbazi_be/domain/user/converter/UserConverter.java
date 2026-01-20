package chungbazi.chungbazi_be.domain.user.converter;

import chungbazi.chungbazi_be.domain.user.dto.response.UserResponseDTO;
import chungbazi.chungbazi_be.domain.user.entity.User;

public class UserConverter {
    public static UserResponseDTO.ProfileDto toProfileDto(User user) {
        return UserResponseDTO.ProfileDto.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .characterImg(user.getCharacterImg())
                .build();
    }
    public static UserResponseDTO.CharacterImgDto toCharacterImgDto(User user) {
        return UserResponseDTO.CharacterImgDto.builder()
                .characterImg(user.getCharacterImg())
                .build();
    }

    public static UserResponseDTO.RewardDto toRewardDto(int rewardLevel, int postCount, int commentCount) {
        return UserResponseDTO.RewardDto.builder()
                .rewardLevel(rewardLevel)
                .postCount(postCount)
                .commentCount(commentCount)
                .build();
    }

    public static UserResponseDTO.EmailExistsDto toEmailExistsDto(boolean isExist) {
        return UserResponseDTO.EmailExistsDto.builder()
                .isExist(isExist)
                .build();
    }
}
