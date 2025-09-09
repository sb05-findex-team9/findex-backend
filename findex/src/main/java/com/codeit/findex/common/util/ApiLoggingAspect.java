package com.codeit.findex.common.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ApiLoggingAspect {

	@Before("within(@org.springframework.web.bind.annotation.RestController com.codeit..*)")
	public void logApiCall(JoinPoint joinPoint) {
		String methodName = joinPoint.getSignature().toShortString();
		System.out.println("API call: " + methodName);
	}

	@AfterReturning(pointcut = "within(@org.springframework.web.bind.annotation.RestController com.codeit..*)", returning = "result")
	public void logApiReturn(JoinPoint joinPoint, Object result) {
		String methodName = joinPoint.getSignature().toShortString();
		System.out.println("API returned: " + methodName + " -> " + result);
	}
}
