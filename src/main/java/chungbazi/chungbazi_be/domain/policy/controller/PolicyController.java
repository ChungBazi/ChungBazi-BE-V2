package chungbazi.chungbazi_be.domain.policy.controller;

import chungbazi.chungbazi_be.domain.document.service.CalendarDocumentService;
import chungbazi.chungbazi_be.domain.policy.dto.*;
import chungbazi.chungbazi_be.domain.policy.entity.Category;
import chungbazi.chungbazi_be.domain.policy.service.PolicyService;
import chungbazi.chungbazi_be.global.apiPayload.ApiResponse;
import chungbazi.chungbazi_be.global.service.PopularSearchService;
import chungbazi.chungbazi_be.global.utils.PaginationResult;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/policies")
@Tag(name = "[정책]", description = "정책 관련 API")
public class PolicyController {

    private final PolicyService policyService;
    private final CalendarDocumentService calendarDocumentService;
    private final PopularSearchService popularSearchService;

    //정책 open api 수동 호출 확인 api
//    public ApiResponse<String> fetchPoliciesManually() {
//        policyService.getPolicy();  // 👈 여기서 강제 호출
//        return ApiResponse.onSuccess("정책 수동 업데이트 완료!");
//    }

    // 정책 검색
    @Operation(summary = "정책 검색 API", description = "정책 검색")
    @GetMapping("/search")
    public ApiResponse<PolicyListResponse> getSearchPolicy(
            @RequestParam(value = "name", required = true) String name,
            @RequestParam(value = "cursor", required = false) String cursor,
            @RequestParam(value = "size", defaultValue = "15", required = false) int size,
            @RequestParam(value = "order", defaultValue = "latest", required = false) String order) {

        PolicyListResponse response = policyService.getSearchPolicy(name, cursor, size, order);
        return ApiResponse.onSuccess(response);
    }

    // 인기 검색어 조회
    @Operation(summary = "인기 검색어 조회 API", description = "인기 검색어 조회")
    @GetMapping("/search/popular")
    public ApiResponse<PopularSearchResponse> getPopularSearch() {
        PopularSearchResponse response = popularSearchService.getPopularSearch("policy");
        return ApiResponse.onSuccess(response);
    }

    // 카테고리별 정책 검색
    @Operation(summary = "카테고리별 정책 API", description = "카테고리별 정책 조회")
    @GetMapping
    public ApiResponse<PaginationResult<PolicyListOneResponse>> getCategoryPolicy(
            @RequestParam(value = "category", required = true) Category category,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "size", defaultValue = "15", required = false) int size,
            @RequestParam(value = "order", defaultValue = "latest", required = false) String order) {

        PaginationResult<PolicyListOneResponse> response = policyService.getCategoryPolicy(category, cursor, size, order);
        return ApiResponse.onSuccess(response);
    }


    // 정책 상세 조회
    @Operation(summary = "정책 상세 조회 API", description = "정책 상세 조회 ")
    @GetMapping("/{policyId}")
    public ApiResponse<PolicyDetailsResponse> getPolicyDetails(@PathVariable Long policyId) {

        PolicyDetailsResponse response = policyService.getPolicyDetails(policyId);
        return ApiResponse.onSuccess(response);
    }

    // 캘린더 정책 전체 조회
    @Operation(summary = "캘린더 정책 전체 조회 API", description = "캘린더 정책 전체 조회")
    @GetMapping("/calendar")
    public ApiResponse<List<PolicyCalendarResponse>> getCalendarList(@RequestParam String yearMonth) {

        List<PolicyCalendarResponse> response = policyService.getCalendarList(yearMonth);
        return ApiResponse.onSuccess(response);
    }

    // 캘린더 정책 상세 조회
    @Operation(summary = "캘린더 정책 상세 조회 API", description = "캘린더 정책 상세 조회")
    @GetMapping("/calendar/{cartId}")
    public ApiResponse<PolicyCalendarDetailResponse> getCalendarDetail(@PathVariable Long cartId) {

        PolicyCalendarDetailResponse response = policyService.getCalendarDetail(cartId);
        return ApiResponse.onSuccess(response);
    }

    // 추천 정책 조회
    @Operation(summary = "추천 정책 조회 API", description = "추천 정책 상세 조회")
    @GetMapping("/recommend")
    public ApiResponse<PolicyRecommendResponse> getRecommendPolicy(@RequestParam Category category,
                                                                   @RequestParam(value = "cursor", required = false) Long cursor,
                                                                   @RequestParam(value = "size", defaultValue = "15", required = false) int size,
                                                                   @RequestParam(value = "order", defaultValue = "latest", required = false) String order) {

        PolicyRecommendResponse response = policyService.getRecommendPolicy(category, cursor, size, order);
        return ApiResponse.onSuccess(response);
    }
}