package chungbazi.chungbazi_be.domain.user.dto.response;

import chungbazi.chungbazi_be.domain.user.entity.mapping.UserInterest;

import java.util.List;
import java.util.stream.Collectors;

public record UserInterestListResponse(
        List<String> userInterestList
) {
    public static UserInterestListResponse from(List<UserInterest> interests) {
        List<String> userInterests = interests.stream()
                .map(userInterest -> userInterest.getInterest().getName())
                .collect(Collectors.toList());

        return new UserInterestListResponse(userInterests);
    }
}
