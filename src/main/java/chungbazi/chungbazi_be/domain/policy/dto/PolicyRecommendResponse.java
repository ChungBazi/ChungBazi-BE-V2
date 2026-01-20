package chungbazi.chungbazi_be.domain.policy.dto;

import chungbazi.chungbazi_be.domain.policy.entity.Category;
import chungbazi.chungbazi_be.domain.policy.entity.Policy;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PolicyRecommendResponse {

    private List<PolicyListOneResponse> policies;
    private Set<Category> interests;
    private boolean isReadAllNotifications;
    private boolean hasNext;
    private String nextCursor;
    private String username;

    public static PolicyRecommendResponse of(List<PolicyListOneResponse> policies, Set<Category> interests, boolean hasNext,
                                             boolean isReadAllNotifications,
                                             String username, String nextCursor) {


        return PolicyRecommendResponse.builder()
                .policies(policies)
                .interests(interests)
                .hasNext(hasNext)
                .username(username)
                .isReadAllNotifications(isReadAllNotifications)
                .nextCursor(nextCursor)
                .build();
    }
}
