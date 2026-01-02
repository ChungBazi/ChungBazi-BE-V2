package chungbazi.chungbazi_be.domain.user.dto.request;

import chungbazi.chungbazi_be.domain.user.entity.enums.Education;
import chungbazi.chungbazi_be.domain.user.entity.enums.Employment;
import chungbazi.chungbazi_be.domain.user.entity.enums.Income;
import chungbazi.chungbazi_be.domain.user.entity.enums.Region;
import chungbazi.chungbazi_be.domain.user.entity.enums.RewardLevel;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class UserRequestDTO {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterDto {
        @NotNull
        @Schema(example = "서울시 강남구", description = "사용자의 지역")
        private Region region;

        @NotNull
        @Schema(example = "재직자", description = "사용자의 고용 상태")
        private Employment employment;

        @NotNull
        @Schema(example = "9분위", description = "사용자의 소득 수준")
        private Income income;

        @NotNull
        @Schema(example = "고등학교 졸업미만", description = "사용자의 교육 수준")
        private Education education;

        @NotNull
        @Schema(example = "[\"일자리\", \"진로\"]", description = "사용자의 관심 분야")
        private List<String> interests;

        @NotNull
        @Schema(example = "[\"중소기업\", \"여성\", \"저소득층\"]", description = "추가 정보")
        private List<String> additionInfo;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfileUpdateDto {
        @NotBlank
        @Size(min = 1, max = 10, message = "닉네임은 10자 이하")
        String name;

        @NotNull
        RewardLevel characterImg;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UpdateDto {
        @Schema(example = "서울시 강남구", description = "사용자의 지역")
        private Region region;

        @Schema(example = "재직자", description = "사용자의 고용 상태")
        private Employment employment;

        @Schema(example = "9분위", description = "사용자의 소득 수준")
        private Income income;

        @Schema(example = "고등학교 졸업미만", description = "사용자의 교육 수준")
        private Education education;

        @Schema(example = "[\"일자리\", \"진로\"]", description = "사용자의 관심 분야")
        private List<String> interests;

        @Schema(example = "[\"중소기업\", \"여성\", \"저소득층\"]", description = "추가 정보")
        private List<String> additionInfo;
    }

}
