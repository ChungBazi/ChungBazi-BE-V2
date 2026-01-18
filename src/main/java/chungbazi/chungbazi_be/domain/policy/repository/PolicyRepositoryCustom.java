package chungbazi.chungbazi_be.domain.policy.repository;

import chungbazi.chungbazi_be.domain.policy.dto.PolicyListOneResponse;
import chungbazi.chungbazi_be.domain.policy.dto.PolicySearchResult;
import chungbazi.chungbazi_be.domain.policy.entity.Category;
import chungbazi.chungbazi_be.domain.policy.entity.Policy;
import com.querydsl.core.Tuple;
import java.util.List;

public interface PolicyRepositoryCustom {

    List<PolicySearchResult> searchPolicyWithName(String keyword, String cursor, int size, String order);

    String generateNextCursor(Tuple policy, String name);

    List<PolicyListOneResponse> getPolicyWithCategory(Category category, Long cursor, int size, String order);

    List<PolicyListOneResponse> findByCategory(Category category, Long cursor, int size, String order);

}