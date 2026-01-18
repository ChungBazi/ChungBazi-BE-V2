package chungbazi.chungbazi_be.domain.policy.service;

import chungbazi.chungbazi_be.domain.policy.dto.YouthPolicyListResponse;
import chungbazi.chungbazi_be.domain.policy.dto.YouthPolicyResponse;
import chungbazi.chungbazi_be.domain.policy.entity.Policy;
import chungbazi.chungbazi_be.domain.policy.validator.PolicyValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyFetchService {

    private final WebClient webclient;
    private final PolicyValidator policyValidator;

    @Value("${webclient.openApiVlak}")
    private String openApiVlak;

    public List<Policy> fetchPolicies(){
        int display = 20;
        int page = 1;
        String srchPolyBizSecd = "003002001";

        LocalDate plusOneYear = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1);

        List<Policy> allPolicies = new ArrayList<>();

        while (true) {
            List<YouthPolicyResponse> policies = callOpenAPI(display, page);

            //정책이 없을 경우
            if (policies == null || policies.isEmpty()) {
                log.warn("✅ 더 이상 가져올 정책이 없어서 종료 (page={})", page);
                break;
            }

            List<Policy> validPolicies = policies.stream()
                    .filter(policy -> policyValidator.isValid(policy, plusOneYear))
                    .map(Policy::toEntity)
                    .collect(Collectors.toList());

            if (!validPolicies.isEmpty()) {
                allPolicies.addAll(validPolicies);
            }

            YouthPolicyResponse lastPolicy = policies.get(policies.size() - 1);
            if (validPolicies.isEmpty() && !policyValidator.isDateValid(lastPolicy, plusOneYear)) {
                log.info("✅ 마지막 페이지 유효 종료 (page={})", page);
                break;
            }

            page++;
        }
        return allPolicies;
    }

    private List<YouthPolicyResponse> callOpenAPI(int display, int page) {
        try {

            String responseBody = webclient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/go/ythip/getPlcy")
                            .queryParam("apiKeyNm", openApiVlak)     // 인증키
                            .queryParam("pageSize", display)         // 출력 건수
                            .queryParam("pageNum", page)             // 조회 페이지
                            .queryParam("rtnType", "json")           // JSON 반환
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)     // text/plain -> String
                    .block();

            if (responseBody == null || responseBody.isEmpty()) {
                log.warn("OpenAPI 응답이 비어있습니다 (page={})", page);
                return Collections.emptyList();
            }

            ObjectMapper objectMapper = new ObjectMapper();

            // YouthPolicyListResponse 전체 DTO로 파싱
            YouthPolicyListResponse listResponse =
                    objectMapper.readValue(responseBody, YouthPolicyListResponse.class);

            // DTO에서 리스트 꺼내기
            if (listResponse == null
                    || listResponse.getResult() == null
                    || listResponse.getResult().getYouthPolicyList() == null
                    || listResponse.getResult().getYouthPolicyList().isEmpty()) {

                log.info("OpenAPI에서 정책 목록이 없습니다 (page={})", page);
                return Collections.emptyList();
            }

            return listResponse.getResult().getYouthPolicyList();

        }  catch (WebClientResponseException.InternalServerError ex) {
            log.warn("⚠️ OpenAPI 500 에러 발생 page={} → 이 페이지 스킵", page);
            return Collections.emptyList();  // 그냥 해당 페이지 스킵
        } catch (WebClientResponseException ex) {
            log.error("❌ OpenAPI 응답 오류 page={} status={}", page, ex.getRawStatusCode());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("❌ OpenAPI 호출/파싱 오류 page={}", page, e);
            return Collections.emptyList();
        }
    }
}
