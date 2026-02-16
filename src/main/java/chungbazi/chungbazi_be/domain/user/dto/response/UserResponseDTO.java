package chungbazi.chungbazi_be.domain.user.dto.response;

import chungbazi.chungbazi_be.domain.user.entity.enums.RewardLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class UserResponseDTO {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ProfileDto {
        Long userId;
        String name;
        String email;
        String oAuthProvider;
        RewardLevel characterImg;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CharacterImgDto {
        RewardLevel characterImg;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class RewardDto {
        int rewardLevel;
        int postCount;
        int commentCount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class EmailExistsDto {
        boolean isExist;
    }
}
