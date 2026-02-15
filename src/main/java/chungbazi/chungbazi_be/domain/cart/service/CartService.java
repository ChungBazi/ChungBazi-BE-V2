package chungbazi.chungbazi_be.domain.cart.service;

import chungbazi.chungbazi_be.domain.auth.jwt.SecurityUtils;
import chungbazi.chungbazi_be.domain.cart.converter.CartConverter;
import chungbazi.chungbazi_be.domain.cart.dto.CartRequestDTO;
import chungbazi.chungbazi_be.domain.cart.dto.CartResponseDTO;
import chungbazi.chungbazi_be.domain.cart.entity.Cart;
import chungbazi.chungbazi_be.domain.cart.repository.CartRepository;
import chungbazi.chungbazi_be.domain.document.repository.CalendarDocumentRepository;
import chungbazi.chungbazi_be.domain.notification.service.NotificationService;
import chungbazi.chungbazi_be.domain.policy.dto.PolicyCalendarResponse;
import chungbazi.chungbazi_be.domain.policy.entity.Category;
import chungbazi.chungbazi_be.domain.policy.entity.Policy;
import chungbazi.chungbazi_be.domain.policy.repository.PolicyRepository;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.support.UserHelper;
import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.BadRequestHandler;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.NotFoundHandler;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final PolicyRepository policyRepository;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final NotificationService notificationService;
    private final UserHelper userHelper;
    private final CalendarDocumentRepository calendarDocumentRepository;

    // 장바구니에 담기
    @Transactional
    public void addPolicyToCart(Long policyId) {

        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.POLICY_NOT_FOUND));

        User user = userHelper.getAuthenticatedUser();

        if (cartRepository.existsByPolicyAndUser(policy, user)) {
            throw new BadRequestHandler(ErrorStatus.ALREADY_EXIST_CART);
        }

        Cart cart = new Cart(policy, user);
        cartRepository.save(cart);

    }

    @Transactional
    public void deletePolicyFromCart(CartRequestDTO.CartDeleteList deleteListDTO) {
        User user = userHelper.getAuthenticatedUser();

        calendarDocumentRepository.deleteByCart_IdIn(deleteListDTO.getDeleteList());
        calendarDocumentRepository.flush(); // for 즉시 삭제

        cartRepository.deleteByUser_IdAndPolicyIds(user.getId(), deleteListDTO.getDeleteList());
    }

    public List<CartResponseDTO.CartPolicyList> getPoliciesFromCart() {

        Long userId = SecurityUtils.getUserId();
        List<Cart> carts = cartRepository.findByUser_Id(userId);

        // 카테고리별로 그룹화
        Map<Category, List<Cart>> grouped = carts.stream()
                .collect(Collectors.groupingBy(cart -> cart.getPolicy().getCategory()));

        //각 카테고리별로 DTO 변환 및 정렬
        return grouped.entrySet().stream()
                .map(entry -> {
                    Category category = entry.getKey();

                    List<CartResponseDTO.CartPolicy> sortedPolicies = entry.getValue().stream()
                            .map(cart -> CartConverter.toCartPolicy(cart.getPolicy()))
                            .sorted(this::compareByDeadline)
                            .toList();

                    return CartConverter.toCartPolicyList(category, sortedPolicies);
                })
                .toList();

    }

    public List<PolicyCalendarResponse> findByUser_IdAndYearMonth(Long userId, int year, int month) {

        List<Cart> carts = cartRepository.findByUser_Id(userId);
        return carts.stream()
                .filter(cart -> {
                    Policy policy = cart.getPolicy();
                    // 상시모집인 경우 필터링
                    if (policy.getStartDate() == null || policy.getEndDate() == null) {
                        return false;
                    }

                    LocalDate startDate = policy.getStartDate();
                    LocalDate endDate = policy.getEndDate();

                    return ((startDate.getYear() == year && startDate.getMonthValue() == month) || (
                            endDate.getYear() == year && endDate.getMonthValue() == month));
                })
                .map(cart -> PolicyCalendarResponse.of(cart.getPolicy(), cart.getId()))
                .toList();
    }

    public Cart findById(Long cartId) {

        return cartRepository.findById(cartId).orElseThrow(() -> new NotFoundHandler(ErrorStatus.NOT_FOUND_CART));
    }

    @Transactional(readOnly = true)
    public List<Cart> getCartsByEndDate(List<LocalDate> targetDates) {
        return cartRepository.findAllByPolicyEndDate(targetDates);
    }

    @Transactional(readOnly = true)
    public List<Cart> findAllByPolicy(Policy policy) {
        return cartRepository.findAllByPolicy(policy);
    }

    @Transactional(readOnly = true)
    public List<Long> findIdsByPolicyIdIn(List<Long> expiredPolicyIds) {
        return cartRepository.findIdsByPolicyIdIn(expiredPolicyIds);
    }

    @Transactional
    public void deleteByPolicyIdIn(List<Long> expiredPolicyIds) {
        cartRepository.deleteByPolicyIdIn(expiredPolicyIds);
    }

    @Transactional
    public void deletePolicyInCart(List<Long> expiredPolicyIds) {
        // 해당 policyIds를 가진 모든 cart 찾기
        if (expiredPolicyIds == null || expiredPolicyIds.isEmpty()) {
            return;
        }

        cartRepository.deleteAllByPolicyIdIn(expiredPolicyIds);
    }

    /*
        마감일 기준 정렬 비교자
        1. 마감 지난 정책 (오래 지난 것 먼저)
        2. 마감 안 지난 정책 (임박한 것 먼저)
        3. 상시 모집 종책
     */
    private int compareByDeadline(CartResponseDTO.CartPolicy policy1, CartResponseDTO.CartPolicy policy2) {
        LocalDate endDate1 = policy1.getEndDate();
        LocalDate endDate2 = policy2.getEndDate();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 1. 상시 모집(null) 처리
        if (endDate1 == null && endDate2 == null) return 0;
        if (endDate1 == null) return 1;   // policy1 상시 → 뒤로
        if (endDate2 == null) return -1;  // policy2 상시 → policy1 앞으로

        int dDay1 = (int) ChronoUnit.DAYS.between(today, endDate1);
        int dDay2 = (int) ChronoUnit.DAYS.between(today, endDate2);

        boolean expired1 = dDay1 < 0;
        boolean expired2 = dDay2 < 0;

        if (expired1 && expired2) {
            // 둘 다 지남 → 오래 지난 것 먼저 (오름차순)
            return Integer.compare(dDay1, dDay2);
        } else if (expired1) {
            // p1만 지남 → p1이 먼저
            return -1;
        } else if (expired2) {
            // p2만 지남 → p2가 먼저
            return 1;
        } else {
            // 둘 다 안 지남 → 임박한 것 먼저 (오름차순)
            return Integer.compare(dDay1, dDay2);
        }
    }
}
