package chungbazi.chungbazi_be.domain.policy.repository;

import chungbazi.chungbazi_be.domain.policy.entity.Category;
import chungbazi.chungbazi_be.domain.policy.entity.Policy;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PolicyRepository extends JpaRepository<Policy, Long>, PolicyRepositoryCustom {

    boolean existsByBizId(String bizId);

    //챗봇 정책찾기용 -> 추후 수정 예정
    List<Policy> findTop5ByCategoryOrderByCreatedAtDesc(Category category);

    @Query("SELECT p.id FROM Policy p " +
            "WHERE p.endDate < :today")
    List<Long> findIdsByEndDateBefore(@Param("today") LocalDate today);

    @Modifying
    @Query("DELETE FROM Policy p " +
            "WHERE p.id IN :expiredPolicyIds")
    long deleteByIdIn(@Param("expiredPolicyIds") List<Long> expiredPolicyIds);
}