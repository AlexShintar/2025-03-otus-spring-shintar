package ru.otus.hw.dto;

import java.util.List;

public record ValidationErrorResponse(List<FieldValidationError> errors) {

    public record FieldValidationError(String field, String message) {
    }
}
