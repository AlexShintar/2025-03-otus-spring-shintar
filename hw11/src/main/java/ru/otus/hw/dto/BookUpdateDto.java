package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BookUpdateDto(
        @NotBlank(message = "Please provide a title")
        @Size(min = 2, max = 255, message = "Title must be between {min} and {max} characters")
        String title,

        @NotNull(message = "Please select an author")
        String authorId,

        @NotNull(message = "Please select at least one genre")
        @Size(min = 1, message = "Please select at least one genre")
        List<@NotNull(message = "Genre id cannot be null") String> genreIds) {
}
