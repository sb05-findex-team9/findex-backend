package com.codeit.findex.common.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException e) {
    return body(HttpStatus.BAD_REQUEST, "잘못된 요청", e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException e) {
    String details = e.getBindingResult().getFieldErrors().stream()
        .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
        .findFirst().orElse("유효성 검사 오류");
    return body(HttpStatus.BAD_REQUEST, "유효성 검사 실패", details);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<Map<String, Object>> tooLarge(MaxUploadSizeExceededException e) {
    return body(HttpStatus.PAYLOAD_TOO_LARGE, "파일 크기 초과", "업로드 크기 제한을 초과했습니다");
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<Map<String, Object>> notFound(NoSuchElementException e) {
    return body(HttpStatus.NOT_FOUND, "리소스를 찾을 수 없음", e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> serverError(Exception e) {
    return body(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류", e.getMessage());
  }

  private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message, String details) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("timestamp", Instant.now().toString());
    map.put("status", status.value());
    map.put("message", message);
    map.put("details", details == null ? "" : details);
    return ResponseEntity.status(status).body(map);
  }
}