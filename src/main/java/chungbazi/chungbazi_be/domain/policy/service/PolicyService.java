package chungbazi.chungbazi_be.domain.policy.service;

import chungbazi.chungbazi_be.domain.auth.jwt.SecurityUtils;
import chungbazi.chungbazi_be.domain.cart.entity.Cart;
import chungbazi.chungbazi_be.domain.cart.service.CartService;
import chungbazi.chungbazi_be.domain.document.entity.CalendarDocument;
import chungbazi.chungbazi_be.domain.document.service.CalendarDocumentService;
import chungbazi.chungbazi_be.domain.notification.service.NotificationService;
import chungbazi.chungbazi_be.domain.policy.dto.*;
import chungbazi.chungbazi_be.domain.policy.entity.Category;
import chungbazi.chungbazi_be.domain.policy.entity.Policy;
import chungbazi.chungbazi_be.domain.policy.repository.PolicyRepository;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.entity.mapping.UserInterest;
import chungbazi.chungbazi_be.domain.user.support.UserHelper;
import chungbazi.chungbazi_be.global.apiPayload.code.status.ErrorStatus;
import chungbazi.chungbazi_be.global.apiPayload.exception.GeneralException;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.BadRequestHandler;
import chungbazi.chungbazi_be.global.apiPayload.exception.handler.NotFoundHandler;
import chungbazi.chungbazi_be.global.utils.PaginationResult;
import chungbazi.chungbazi_be.global.utils.PaginationUtil;
import chungbazi.chungbazi_be.global.utils.PopularSearch;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PolicyService {

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

        User user = userHelper.getAuthenticatedUser();

        if (name == null) {
            throw new GeneralException(ErrorStatus.NO_SEARCH_NAME);
        }

        // 인기 검색어에 반영
        popularSearch.updatePopularSearch(name, "policy");

        // 검색 결과 반환
        List<PolicySearchResult> searchResults = policyRepository.searchPolicyWithName(name, cursor, size + 1, order);

        //페이징 처리
        String nextCursor = null;
        boolean hasNext = searchResults.size() > size;

        if (hasNext) {
            PolicySearchResult lastItem = searchResults.get(size - 1);
            nextCursor = policyRepository.generateSearchCursor(lastItem, order);

            searchResults = searchResults.subList(0, size);
        }

        //프론트 통신용 응답으로 변환
        List<PolicyListOneResponse> responses = searchResults.stream()
                .map(PolicySearchResult::toResponse)
                .collect(Collectors.toList());

        return PolicyListResponse.of(responses, nextCursor, hasNext);
    }

    // 카테고리별 정책 조회
    public PolicyListResponse getCategoryPolicy(Category categoryName, String cursor, int size, String order) {

        User user = userHelper.getAuthenticatedUser();

        List<PolicyListOneResponse> policies = policyRepository.getPolicyWithCategory(categoryName, cursor, size + 1, order);

        String nextCursor = null;
        boolean hasNext = policies.size() > size;

        if (hasNext) {
            PolicyListOneResponse lastItem = policies.get(size - 1);
            nextCursor = policyRepository.generateCategoryCursor(lastItem, order);

            policies = policies.subList(0, size);
        }

        return PolicyListResponse.of(policies, nextCursor, hasNext);

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

    //추천 정책 조회
    public PolicyRecommendResponse getRecommendPolicy(Category category, String cursor, int size, String order) {

        User user = userHelper.getAuthenticatedUser();

        List<PolicyListOneResponse> policies = policyRepository.getPolicyWithCategory(category, cursor, size+1, order);

        String nextCursor = null;
        boolean hasNext = policies.size() > size;

        if (hasNext) {
            PolicyListOneResponse lastItem = policies.get(size - 1);
            nextCursor = policyRepository.generateCategoryCursor(lastItem, order);

            policies = policies.subList(0, size);
        }

        //안 읽은 알림 개수 & 유저 관심분야
        boolean isReadAllNotifications=notificationService.isReadAllNotification();
        Set<Category> userCategories = getUserInterests(user);

        return PolicyRecommendResponse.of(policies, userCategories, hasNext, isReadAllNotifications, user.getName(), nextCursor);
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

        // Cart와의 연관관계 제거
        cartService.nullifyPolicyInCart(expiredPolicyIds);

        // Policy 삭제
        long deletedPolicies = policyRepository.deleteByIdIn(expiredPolicyIds);

        log.info("🧹 삭제된 정책 수: {}", deletedPolicies);
        return deletedPolicies;

    }
}