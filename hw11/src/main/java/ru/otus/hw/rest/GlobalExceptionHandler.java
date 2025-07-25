package ru.otus.hw.rest;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAny(Exception ex, HttpServletRequest request) {
        if (isHtmlRequest(request)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found", ex);
        }

        String errorId = UUID.randomUUID().toString();
        log.error("Internal error occurred. ErrorId={}", errorId, ex);
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
