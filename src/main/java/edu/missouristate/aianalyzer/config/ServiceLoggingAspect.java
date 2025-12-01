package edu.missouristate.aianalyzer.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
@Slf4j
@Aspect
@Component
public class ServiceLoggingAspect {
    @Pointcut("within(edu.missouristate.aianalyzer..*) && @within(org.springframework.stereotype.Service)")
    public void serviceMethods() {}
    @Around ("serviceMethods()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return pjp.proceed();
        } catch (Exception ex) {
            log.error("service error in {}: {}", pjp.getSignature().toShortString(), ex.getMessage(), ex);
            throw ex;
        } finally {
            long ms = System.currentTimeMillis() - start;
            log.debug("service {} took {} ms", pjp.getSignature().toShortString(), ms);
        }
    }
}
