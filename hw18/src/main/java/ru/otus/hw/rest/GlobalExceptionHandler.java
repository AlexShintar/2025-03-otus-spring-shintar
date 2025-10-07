package ru.otus.hw.rest;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;
import ru.otus.hw.dto.ValidationErrorResponse;
import ru.otus.hw.exceptions.EntityNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation failed: {}", ex.getMessage(), ex);
        List<ValidationErrorResponse.FieldValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> new ValidationErrorResponse.FieldValidationError(
                        err.getField(),
                        err.getDefaultMessage()))
                .collect(Collectors.toList());
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(errors));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleNotFound(EntityNotFoundException ex) {
        log.error("Entity not found: {}", ex.getMessage(), ex);
        return ResponseEntity.status(404)
                .body(Map.of("message", "Requested object not found"));
    }

    // ✅ добавили обработчики для Resilience4j сценариев
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<?> handleRateLimiter(RequestNotPermitted ex) {
        log.error("Rate limiter triggered: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("message", "Too many requests. Please try again later."));
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<?> handleCircuitBreaker(CallNotPermittedException ex) {
        log.error("Circuit breaker open: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Service temporarily unavailable. Please try again later."));
    }

    @ExceptionHandler(Exception.class)
    public Object handleAny(Exception ex, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.error("Internal error at {}. ErrorId={}", request.getRequestURI(), errorId, ex);

        if (isHtmlRequest(request)) {
            ModelAndView modelAndView = new ModelAndView("error");
            modelAndView.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            modelAndView.addObject("message", "Something went wrong");
            modelAndView.addObject("errorId", errorId);
            return modelAndView;
        }

        return ResponseEntity.status(500)
                .body(Map.of(
                        "message", "Internal server error. Please try again later.",
                        "errorId", errorId
                ));
    }

    private boolean isHtmlRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("text/html");
    }
}
