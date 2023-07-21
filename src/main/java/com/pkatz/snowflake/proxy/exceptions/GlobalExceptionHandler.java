package com.pkatz.snowflake.proxy.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity handleInvalidRequests(MethodArgumentNotValidException ex) {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", new Date());
        body.put("status", ex.getStatusCode().value());
        List<String> errors = ex.getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.toList());
        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }
}
