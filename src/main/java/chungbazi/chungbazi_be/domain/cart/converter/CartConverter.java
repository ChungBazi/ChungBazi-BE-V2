package chungbazi.chungbazi_be.domain.cart.converter;

import chungbazi.chungbazi_be.domain.cart.dto.CartResponseDTO;
import chungbazi.chungbazi_be.domain.cart.dto.CartResponseDTO.CartPolicy;
import chungbazi.chungbazi_be.domain.policy.entity.Category;
import chungbazi.chungbazi_be.domain.policy.entity.Policy;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class CartConverter {

    public static CartResponseDTO.CartPolicy toCartPolicy(Policy policy) {
        Integer dDay;
        if (policy.getEndDate() == null) {
            dDay = null;
        } else {
            dDay = (int) ChronoUnit.DAYS.between(LocalDate.now(), policy.getEndDate());
        }
        return CartResponseDTO.CartPolicy.builder()
                .name(policy.getName())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .dDay(dDay)
                .policyId(policy.getId())
                .build();
    }

    public static CartResponseDTO.CartPolicyList toCartPolicyList(Category category, List<CartPolicy> policyDetails) {
        return CartResponseDTO.CartPolicyList.builder()
                .categoryName(category.getKoreanName())
                .cartPolicies(policyDetails)
                .build();
    }
}
