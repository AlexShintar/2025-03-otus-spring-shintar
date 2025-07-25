package ru.otus.hw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ValidationErrorResponse {
    private List<FieldValidationError> errors;

    @Data
    @AllArgsConstructor
    public static class FieldValidationError {
        private String field;

        private String message;
    }
}
