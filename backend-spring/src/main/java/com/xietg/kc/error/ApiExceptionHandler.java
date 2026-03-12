package com.xietg.kc.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex) {
        ApiErrorResponse body = new ApiErrorResponse(
                ex.getCode(),
                ex.getMessage(),
                ex.getParams()
        );

        return ResponseEntity
                .status(ex.getStatus())
                .body(body);
    }

    public record ApiErrorResponse(
            String code,
            String message,
            Object params
    ) {}
}