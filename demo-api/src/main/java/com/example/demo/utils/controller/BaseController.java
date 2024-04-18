package com.example.demo.utils.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BaseController {
	
	protected static final String ERROR_RESPONSE_400 = """
        {
            "error": "Invalid input",
            "message": "Age must be a positive integer"
        }
        """;

    protected static final String ERROR_RESPONSE_500 = """
        {
            "error": "Internal server error",
            "message": "An unexpected error occurred"
        }
        """;

    
    /**
     * 處理 JSON 格式錯誤的請求體異常
     * 當請求體的 JSON 格式不正確時，返回 400 狀態碼和錯誤訊息
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid input", "Invalid JSON format in request body");
    }

    /**
     * 處理所有其他異常
     * 當發生未知的錯誤時，返回 500 狀態碼和錯誤訊息
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handleException(Exception ex) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", "An unexpected error occurred");
    }
    
    /**
     * 處理輸入驗證失敗的異常
     * 當輸入的數據不符合驗證規則時，返回 400 狀態碼和錯誤訊息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid input");
        errorResponse.put("message", ex.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining(", ")));
        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 創建錯誤回應
     * 根據給定的狀態碼、錯誤代碼和錯誤訊息，創建一個包含錯誤回應的 ResponseEntity 對象
     */
    protected ResponseEntity<?> createErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        return new ResponseEntity<>(errorResponse, status);
    }
    
}
