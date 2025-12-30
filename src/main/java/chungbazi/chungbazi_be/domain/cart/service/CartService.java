package chungbazi.chungbazi_be.domain.cart.service;

import chungbazi.chungbazi_be.domain.auth.jwt.SecurityUtils;
import chungbazi.chungbazi_be.domain.cart.converter.CartConverter;
import chungbazi.chungbazi_be.domain.cart.dto.CartRequestDTO;
import chungbazi.chungbazi_be.domain.cart.dto.CartResponseDTO;
import chungbazi.chungbazi_be.domain.cart.entity.Cart;
import chungbazi.chungbazi_be.domain.cart.repository.CartRepository;
import chungbazi.chungbazi_be.domain.document.repository.CalendarDocumentRepository;
import chungbazi.chungbazi_be.domain.notification.dto.internal.NotificationData;
import chungbazi.chungbazi_be.domain.notification.entity.enums.NotificationType;
import chungbazi.chungbazi_be.domain.notification.service.NotificationService;
import chungbazi.chungbazi_be.domain.policy.dto.PolicyCalendarResponse;
import chungbazi.chungbazi_be.domain.policy.entity.Category;
import chungbazi.chungbazi_be.domain.policy.entity.Policy;
import chungbazi.chungbazi_be.domain.policy.repository.PolicyRepository;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.service.UserService;
import chungbazi.chungbazi_be.domain.user.utils.UserHelper;
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
    private final UserService userService;
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

        Long userId = SecurityUtils.getUserId();
        User user = userService.findByUserId(userId);

        calendarDocumentRepository.deleteByCart_IdIn(deleteListDTO.getDeleteList());
        calendarDocumentRepository.flush(); // for 즉시 삭제

        cartRepository.deleteByUser_IdAndPolicyIds(userId, deleteListDTO.getDeleteList());
    }

    public List<CartResponseDTO.CartPolicyList> getPoliciesFromCart() {

        Long userId = SecurityUtils.getUserId();
        List<Cart> carts = cartRepository.findByUser_Id(userId);

        // 카테고리별로 그룹화
        Map<Category, List<Cart>> grouped = carts.stream()
                .collect(Collectors.groupingBy(cart -> cart.getPolicy().getCategory()));

        List<CartResponseDTO.CartPolicyList> groupedDTO = grouped.entrySet().stream().map(entry -> {
                    Category category = entry.getKey();
                    List<Cart> cartList = entry.getValue();

                    List<CartResponseDTO.CartPolicy> policyDetails = cartList.stream()
                            .map(cart -> {
                                Policy policy = cart.getPolicy();
                                return CartConverter.toCartPolicy(policy);
                            })
                            .sorted((p1, p2) -> {
                                LocalDate endDate1 = p1.getEndDate();
                                LocalDate endDate2 = p2.getEndDate();

                                // 1. 상시 모집(endDate가 null인 경우)을 뒤로 정렬
                                if (endDate1 == null && endDate2 == null) {
                                    return 0;  // 둘 다 상시 모집이면 순서를 변경하지 않음
                                } else if (endDate1 == null) {
                                    return 1;  // p1이 상시 모집 -> 뒤로 보냄
                                } else if (endDate2 == null) {
                                    return -1; // p2가 상시 모집 -> p1이 앞에 옴
                                }

                                // 2. dDay 계산
                                int dDay1 = (int) ChronoUnit.DAYS.between(LocalDate.now(), endDate1);
                                int dDay2 = (int) ChronoUnit.DAYS.between(LocalDate.now(), endDate2);

                                // 3. 정렬 조건
                                if (dDay1 < 0 && dDay2 < 0) {
                                    // 둘 다 dDay가 음수인 경우: 오름차순 정렬
                                    return Integer.compare(dDay1, dDay2);
                                } else if (dDay1 < 0) {
                                    // dDay1은 음수, dDay2는 양수 -> dDay1이 우선
                                    return -1;
                                } else if (dDay2 < 0) {
                                    // dDay1이 양수, dDay2는 음수 -> dDay2가 우선
                                    return 1;
                                } else {
                                    // 둘 다 양수인 경우: 오름차순 정렬
                                    return Integer.compare(dDay1, dDay2);
                                }
                            })
                            .toList();

                    return CartConverter.toCartPolicyList(category, policyDetails);
                })
                .toList();

        return groupedDTO;

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

    @Transactional
    public List<Cart> getCartsByEndDate(List<LocalDate> targetDates) {
        return cartRepository.findAllByPolicyEndDate(targetDates);
    }
}
