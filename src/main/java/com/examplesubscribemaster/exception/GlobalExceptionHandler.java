package com.examplesubscribemaster.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ErrorResponse> r(HttpStatus s, String e, String m, HttpServletRequest q) {
        return ResponseEntity.status(s).body(new ErrorResponse(Instant.now(), s.value(), e, m, q.getRequestURI()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ErrorResponse> nf(ResourceNotFoundException e, HttpServletRequest q) {
        return r(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", e.getMessage(), q);
    }

    @ExceptionHandler(UnauthorizedException.class)
    ResponseEntity<ErrorResponse> ua(UnauthorizedException e, HttpServletRequest q) {
        return r(HttpStatus.FORBIDDEN, "UNAUTHORIZED", e.getMessage(), q);
    }

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ErrorResponse> be(BusinessException e, HttpServletRequest q) {
        return r(HttpStatus.CONFLICT, "BUSINESS_ERROR", e.getMessage(), q);
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ErrorResponse> ae(AuthenticationException e, HttpServletRequest q) {
        return r(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password", q);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> ve(MethodArgumentNotValidException e, HttpServletRequest q) {
        String m = e.getBindingResult().getFieldErrors().stream().map(x -> x.getField() + ": " + x.getDefaultMessage()).collect(Collectors.joining("; "));
        return r(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", m, q);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> nr(HttpMessageNotReadableException e, HttpServletRequest q) {
        return r(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Malformed request body", q);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> all(Exception e, HttpServletRequest q) {
        log.error("Unhandled exception on {} {}", q.getMethod(), q.getRequestURI(), e);
        return r(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Unexpected server error", q);
    }
}
