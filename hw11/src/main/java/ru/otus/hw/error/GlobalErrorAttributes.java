package ru.otus.hw.error;

import jakarta.validation.ConstraintViolationException;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;
import ru.otus.hw.exceptions.EntityNotFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class GlobalErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request,
                                                  ErrorAttributeOptions options) {
        Throwable error = getError(request);
        HttpStatus status = resolveStatus(error);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("path", request.path());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", resolveMessage(error, status));
        body.put("errorId", UUID.randomUUID().toString());
        body.put("requestId", request.exchange().getRequest().getId());
        return body;
    }

    private HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException rse) {
            return (rse.getStatusCode() instanceof HttpStatus http)
                    ? http
                    : HttpStatus.valueOf(rse.getStatusCode().value());
        }
        if (ex instanceof EntityNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (ex instanceof ConstraintViolationException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (ex instanceof WebExchangeBindException
                || ex instanceof IllegalArgumentException
                || ex instanceof NumberFormatException) {
            return HttpStatus.BAD_REQUEST;
        }
        if (ex instanceof DataIntegrityViolationException) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveMessage(Throwable ex, HttpStatus status) {
        if (ex == null) {
            return "Something went wrong";
        }

        String rse = messageFromResponseStatusException(ex);
        if (rse != null) {
            return rse;
        }

        String validation = messageFromConstraintViolation(ex);
        if (validation != null) {
            return validation;
        }

        String direct = nonBlank(ex.getMessage());
        if (direct != null) {
            return direct;
        }

        return fallbackByStatus(status, ex);
    }

    private String messageFromResponseStatusException(Throwable ex) {
        if (ex instanceof ResponseStatusException rse) {
            String reason = nonBlank(rse.getReason());
            return reason;
        }
        return null;
    }

    private String messageFromConstraintViolation(Throwable ex) {
        if (ex instanceof ConstraintViolationException cve) {
            return cve.getConstraintViolations().stream()
                    .map(v -> {
                        var path = v.getPropertyPath();
                        var prefix = (path == null || path.toString().isBlank()) ? "" : path + ": ";
                        return prefix + v.getMessage();
                    })
                    .sorted()
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Validation failed");
        }
        return null;
    }

    private String fallbackByStatus(HttpStatus status, Throwable ex) {
        if (status == null) {
            return "Something went wrong";
        }
        return switch (status) {
            case NOT_FOUND -> "Resource not found";
            case BAD_REQUEST -> "Validation failed";
            case CONFLICT -> "Conflict";
            case FORBIDDEN -> "Forbidden";
            default -> ex.getClass().getSimpleName();
        };
    }

    private String nonBlank(String s) {
        if (s == null) {
            return null;
        }
        if (s.isBlank()) {
            return null;
        }
        return s;
    }
}
