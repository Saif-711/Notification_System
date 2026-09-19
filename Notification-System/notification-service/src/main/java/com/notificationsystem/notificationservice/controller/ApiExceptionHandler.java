package com.notificationsystem.notificationservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * ApiExceptionHandler = a global "catch errors from controllers" class.
 *
 * Beginner view: if the client forgets userId or message,
 * @Valid throws MethodArgumentNotValidException.
 * Without this class, Spring would return a long ugly error.
 * Here we return a simple 400 Bad Request.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid notification request"));
    }
}
