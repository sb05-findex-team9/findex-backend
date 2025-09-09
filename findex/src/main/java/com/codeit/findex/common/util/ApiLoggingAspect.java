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

// 데이터 목록 조회 + 데이터 등록 + 차트 조회 + 성과 랭킹 조회 + 연동 작업 목록 조회 (지인)
// 데이터 삭제 + 데이터 수정 + 관심 지수 성과 조회 + 지수 데이터 CSV export + 지수 정보 연동 + 지수 데이터 연동 (수연)
