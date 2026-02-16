package chungbazi.chungbazi_be.domain.user.dto.response;

import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.entity.mapping.UserInterest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record UserInformationResponse(
    String employment,
    String education,
    String income,
    List<String> interests,
    List<String> additions
) {
    public static UserInformationResponse from(User user) {
        List<String> interests = user.getUserInterestList().stream()
                .map(userInterest -> userInterest.getInterest().getName())
                .collect(Collectors.toList());

        List<String> additions = user.getUserAdditionList().stream()
                .map(userAddition -> userAddition.getAddition().getName())
                .collect(Collectors.toList());

        return new UserInformationResponse(
                user.getEmployment().toJson(),
                user.getEducation().toJson(),
                user.getIncome().toJson(),
                interests,
                additions
        );
    }
}
