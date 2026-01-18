package chungbazi.chungbazi_be.domain.policy.service;

import chungbazi.chungbazi_be.domain.auth.jwt.SecurityUtils;
import chungbazi.chungbazi_be.domain.cart.entity.Cart;
import chungbazi.chungbazi_be.domain.cart.service.CartService;
import chungbazi.chungbazi_be.domain.document.entity.CalendarDocument;
import chungbazi.chungbazi_be.domain.document.service.CalendarDocumentService;
import chungbazi.chungbazi_be.domain.notification.service.NotificationService;
import chungbazi.chungbazi_be.domain.policy.dto.PolicyCalendarDetailResponse;
import chungbazi.chungbazi_be.domain.policy.dto.PolicyCalendarResponse;
import chungbazi.chungbazi_be.domain.policy.dto.PolicyDetailsResponse;
import chungbazi.chungbazi_be.domain.policy.dto.PolicyListOneResponse;
import chungbazi.chungbazi_be.domain.policy.dto.PolicyListResponse;
import chungbazi.chungbazi_be.domain.policy.dto.PolicyRecommendResponse;
import chungbazi.chungbazi_be.domain.policy.entity.Category;
import chungbazi.chungbazi_be.domain.policy.entity.Policy;
import chungbazi.chungbazi_be.domain.policy.entity.QPolicy;
import chungbazi.chungbazi_be.domain.policy.repository.PolicyRepository;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.entity.enums.Employment;
import chungbazi.chungbazi_be.domain.user.entity.mapping.UserInterest;
import chungbazi.chungbazi_be.domain.user.support.UserHelper;
import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import chungbazi.chungbazi_be.global.apiPayload.exception.GeneralException;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.BadRequestHandler;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.NotFoundHandler;
import chungbazi.chungbazi_be.global.utils.PopularSearch;
import com.querydsl.core.Tuple;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PolicyService {

    private final WebClient webclient;
    private final PolicyRepository policyRepository;
    private final CartService cartService;
    private final CalendarDocumentService calendarDocumentService;
    private final PopularSearch popularSearch;
    private final UserHelper userHelper;
    private final NotificationService notificationService;

    @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
    public void savePolicies(List<Policy> policies) {
        policyRepository.saveAll(policies);
    }


    // 정책 검색
    public PolicyListResponse getSearchPolicy(String name, String cursor, int size, String order) {

        if (name == null) {
            throw new GeneralException(ErrorStatus.NO_SEARCH_NAME);
        }

        // 인기 검색어에 반영
        popularSearch.updatePopularSearch(name, "policy");

        // 검색 결과 반환
        List<Tuple> policies = policyRepository.searchPolicyWithName(name, cursor, size + 1, order);

        String nextCursor = null;
        boolean hasNext = policies.size() > size;

        if (hasNext) {
            policies.subList(0, size);

            Tuple lastTuple = policies.get(policies.size() - 1);
            nextCursor = policyRepository.generateNextCursor(lastTuple, name);
        }

        List<PolicyListOneResponse> policyDtoList = new ArrayList<>();
        for (Tuple tuple : policies) {
            Policy policy = tuple.get(QPolicy.policy);
            policyDtoList.add(PolicyListOneResponse.from(policy));
        }

        if (policies.isEmpty()) {
            return PolicyListResponse.of(policyDtoList, null, false);
        }

        return PolicyListResponse.of(policyDtoList, nextCursor, hasNext);
    }

    // 카테고리별 정책 조회
    public PolicyListResponse getCategoryPolicy(Category categoryName, Long cursor, int size, String order) {

        List<Policy> policies = policyRepository.getPolicyWithCategory(categoryName, cursor, size + 1, order);

        boolean hasNext = policies.size() > size;

        if (hasNext) {
            policies.subList(0, size);
        }

        List<PolicyListOneResponse> policyDtoList = policies.stream().map(PolicyListOneResponse::from).toList();

        return PolicyListResponse.of(policyDtoList, hasNext);
    }

    // 정책상세조회
    public PolicyDetailsResponse getPolicyDetails(Long policyId) {

        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new NotFoundHandler(ErrorStatus.POLICY_NOT_FOUND));
        return PolicyDetailsResponse.from(policy);
    }

    // 캘린더 정책 전체 조회
    public List<PolicyCalendarResponse> getCalendarList(String yearMonth) {

        //유효한 타입인지 검증
        validateYearMonth(yearMonth);

        YearMonth parsedYearMonth = YearMonth.parse(yearMonth, DateTimeFormatter.ofPattern("yyyy-M"));
        int year = parsedYearMonth.getYear();
        log.info("파싱 확인 year: "+year);
        int month = parsedYearMonth.getMonthValue();
        log.info("파싱 확인 month: "+month);

        Long userId = SecurityUtils.getUserId();
        return cartService.findByUser_IdAndYearMonth(userId, year, month);
    }

    // 유효한 타입인지 확인
    private void validateYearMonth(String yearMonth) {
        try {
            YearMonth.parse(yearMonth); // "2025-01" 형식이 아닌 경우 예외 발생
        } catch (DateTimeParseException e) {
            // 유효하지 않은 형식인 경우 CustomException 던지기
            throw new BadRequestHandler(ErrorStatus.NOT_VALID_TYPE_YEAR_MONTH);
        }
    }

    public PolicyCalendarDetailResponse getCalendarDetail(Long cartId) {

        Cart cart = cartService.findById(cartId);
        Policy policy = cart.getPolicy();
        List<CalendarDocument> documents = calendarDocumentService.findAllByCart_Id(cartId);

        return PolicyCalendarDetailResponse.of(cart, policy, documents);
    }

    public PolicyRecommendResponse getRecommendPolicy(Category category, Long cursor, int size, String order) {

        User user = userHelper.getAuthenticatedUser();

        Employment employment = user.getEmployment();

        Set<Category> userCategories = getUserInterests(user);
        List<Policy> policies = policyRepository.findByCategory(category, cursor, size, order);
        List<Policy> filteredPolicies = policies.stream()
                .filter(policy -> policy.getEmployment() == null || employment.getDescription()
                        .equals(policy.getEmployment()))
                .toList();
        boolean hasNext = filteredPolicies.size() > size;
        if (hasNext) {
            filteredPolicies = filteredPolicies.subList(0, size);
        }

        boolean isReadAllNotifications=notificationService.isReadAllNotification();

        return PolicyRecommendResponse.of(policies, userCategories, hasNext,isReadAllNotifications, user.getName());
    }


    private Set<Category> getUserInterests(User user) {
        List<UserInterest> userInterests = user.getUserInterestList();

        Set<Category> userCategories = new HashSet<>();

        Set<String> jobs = new HashSet<>(Arrays.asList("일자리", "창업", "진로", "대외활동"));
        Set<String> housing = new HashSet<>(Arrays.asList("주거", "금융", "생활지원"));
        Set<String> education = new HashSet<>(Arrays.asList("문화예술", "대외활동", "금융", "마음건강", "생활지원", "신체건강"));
        Set<String> culture = new HashSet<>(Arrays.asList("문화예술", "대외활동", "금융", "마음건강", "생활지원", "신체건강"));
        Set<String> rights = new HashSet<>(Arrays.asList("대외활동", "금융", "생활지원"));

        userInterests.forEach(userInterest -> {
            String target = userInterest.getInterest().getName();

            if (jobs.contains(target)) {
                userCategories.add(Category.JOBS);
            }
            if (housing.contains(target)) {
                userCategories.add(Category.HOUSING);
            }
            if (education.contains(target)) {
                userCategories.add(Category.EDUCATION);
            }
            if (culture.contains(target)) {
                userCategories.add(Category.WELFARE_CULTURE);
            }
            if (rights.contains(target)) {
                userCategories.add(Category.PARTICIPATION_RIGHTS);
            }
        });
        return userCategories;
    }

    @Transactional
    public long deleteExpiredPolicies() {

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 1. 삭제 대상 정책 조회
        List<Long> expiredPolicyIds = policyRepository.findIdsByEndDateBefore(today);

        if (expiredPolicyIds.isEmpty()) {
            log.info("📌 삭제 대상 정책 없음.");
            return 0;
        }

        // CalendarDocument 삭제
        List<Long> cartIds = cartService.findIdsByPolicyIdIn(expiredPolicyIds);
        if (!cartIds.isEmpty()) {
            calendarDocumentService.deleteByCartIdIn(cartIds);
        }

        // Cart 삭제
        cartService.deleteByPolicyIdIn(expiredPolicyIds);

        // Policy 삭제
        long deletedPolicies = policyRepository.deleteByIdIn(expiredPolicyIds);

        log.info("🧹 삭제된 정책 수: {}", deletedPolicies);
        return deletedPolicies;

    }
}