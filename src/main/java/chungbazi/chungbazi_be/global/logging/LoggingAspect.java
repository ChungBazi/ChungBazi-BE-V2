package chungbazi.chungbazi_be.global.logging;

import chungbazi.chungbazi_be.domain.policy.dto.PolicyDetailsResponse;
import chungbazi.chungbazi_be.domain.policy.dto.PolicyListResponse;
import chungbazi.chungbazi_be.domain.user.entity.User;
import chungbazi.chungbazi_be.domain.user.support.UserHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {

    private final ObjectMapper objectMapper;
    private static final Logger eventLogger = LoggerFactory.getLogger("USER_EVENT_LOGGER");
    private final UserHelper userHelper;

    @Pointcut("execution(* chungbazi.chungbazi_be.domain..*Controller.*(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        //API 정보
        String methodName = joinPoint.getSignature().getName();
        String packageName = joinPoint.getSignature().getDeclaringTypeName();

        Object result;
        try {
            result = joinPoint.proceed();

        } catch (Exception ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("🚨ERROR | method = {}.{} | message = {} | time = {}ms", packageName, methodName, ex.getMessage(), executionTime);
            throw ex;
        }

        long endTime = System.currentTimeMillis();
        long timeinMs = endTime - startTime;
        log.info("✅SUCCESS | method = {}.{} | time = {}ms", packageName, methodName,timeinMs);
        return result;
    }

    @Around("@annotation(trackEvent)")
    public Object around(ProceedingJoinPoint joinPoint, TrackEvent trackEvent) throws Throwable {
        long startTime = System.currentTimeMillis();

        Map<String, Object> params = getMethodParameters(joinPoint);

        Object result;
        try {
            result = joinPoint.proceed();
            long timeInMs = System.currentTimeMillis() - startTime;

            //메서드 실행 후: 결과값과 합쳐서 이벤트 로그 전송
            sendEventLog(trackEvent.name(), params, result, timeInMs);

            return result;
        } catch (Exception ex) {
            log.error("Event Method Error: {}", ex.getMessage());
            throw ex;
        }
    }

    private void sendEventLog(String eventName, Map<String, Object> params, Object result, long time) {
        try {
            // 결과 개수 추출 (PolicyListResponse 등에서)
            int resultCount = getResultCount(result);

            // 검색 결과가 0개면 'empty_policy_result'로 이벤트명 변경
            String finalEventName = (resultCount == 0 && result instanceof PolicyListResponse )
                    ? "empty_policy_result" : eventName;

            Map<String, Object> extraProperties = new HashMap<>();
            extraProperties.put("input_params", params);
            extraProperties.put("result_count", resultCount);

            Long userId = null;
            try {
                User user = userHelper.getAuthenticatedUser();
                if (user != null) {
                    userId = user.getId();
                    extraProperties.put("user_income", user.getIncome()); // 소득분위
                    extraProperties.put("user_region", user.getRegion()); // 유저 지역
                    extraProperties.put("user_education", user.getEducation()); //유저 학력
                    extraProperties.put("user_employment", user.getEmployment()); //유저 취업 상태
                }
            } catch (Exception e) {
                // 인증 정보가 없는 경우 무시
            }

            if ("policy_detail_view".equals(eventName) && result instanceof PolicyDetailsResponse detail) {
                extraProperties.put("policy_name", detail.getName());
                extraProperties.put("policy_category", detail.getCategoryName());
            }

            UserEventLog logEntry = UserEventLog.builder()
                    .eventName(finalEventName)
                    .traceId(MDC.get("trace_id"))
                    .entryPoint(eventName)
                    .timeStamp(LocalDateTime.now().toString())
                    .userId(userId)
                    .properties(extraProperties)
                    .build();

            eventLogger.info(objectMapper.writeValueAsString(logEntry));
        } catch (Exception e) {
            log.warn("Event Logging Failed: {}", e.getMessage());
        }

    }

    private Map<String, Object> getMethodParameters(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < parameterNames.length; i++) {

            if (!"cursor".equals(parameterNames[i]) && !"size".equals(parameterNames[i])) {
                params.put(parameterNames[i], args[i]);
            }
        }
        return params;
    }

    private int getResultCount(Object result) {
        if (result instanceof PolicyListResponse) {
            return ((PolicyListResponse) result).getPolicies().size();
        }
        return 0;
    }

}
