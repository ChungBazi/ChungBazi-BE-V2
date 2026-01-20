package chungbazi.chungbazi_be.domain.cart.repository;

import chungbazi.chungbazi_be.domain.cart.entity.Cart;
import chungbazi.chungbazi_be.domain.policy.entity.Policy;
import chungbazi.chungbazi_be.domain.user.entity.User;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @Modifying  // 데이터 변경 (update, delete)
    @Query("DELETE from Cart c WHERE c.user.id=:userId AND c.policy.id IN :policyIds")
    void deleteByUser_IdAndPolicyIds(@Param("userId") Long userId, @Param("policyIds") List<Long> policyIds);

    List<Cart> findByUser_Id(Long userId);

    boolean existsByPolicyAndUser(Policy policy, User user);

    @Query("SELECT c FROM Cart c " +
            "JOIN FETCH c.user u " +
            "JOIN FETCH u.notificationSetting " +
            "JOIN FETCH c.policy p " +
            "WHERE p.endDate IN :targetDates ")
    List<Cart> findAllByPolicyEndDate(List<LocalDate> targetDates);

    List<Cart> findAllByPolicy(Policy policy);

    @Query("SELECT c.id FROM Cart c " +
            "WHERE c.policy.id IN :expiredPolicyIds")
    List<Long> findIdsByPolicyIdIn(@Param("expiredPolicyIds") List<Long> expiredPolicyIds);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.policy.id IN :policyIds")
    void deleteByPolicyIdIn(@Param("policyIds") List<Long> policyIds);

    @Query("SELECT c FROM Cart c WHERE c.policy.id IN :expiredPolicyIds")
    List<Cart> findAllByPolicyIdIn(@Param("expiredPolicyIds") List<Long> expiredPolicyIds);

    @Modifying
    @Query("UPDATE Cart c SET c.policy = null WHERE c.policy.id IN :policyIds")
    void nullifyPolicyByPolicyIds(@Param("policyIds") List<Long> policyIds);
}
