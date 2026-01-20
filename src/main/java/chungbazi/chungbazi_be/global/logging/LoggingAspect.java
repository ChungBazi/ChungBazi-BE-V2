package chungbazi.chungbazi_be.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;


@Aspect
@Component
@Slf4j
public class LoggingAspect {

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


}
