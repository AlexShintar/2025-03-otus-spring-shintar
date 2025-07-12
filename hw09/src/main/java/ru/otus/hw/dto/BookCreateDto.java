package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookCreateDto {
    @NotBlank(message = "Please provide a title")
    @Size(min = 2, max = 255, message = "Title must be between {min} and {max} characters")
    private String title;

    @NotNull(message = "Please select an author")
    private Long authorId;

    @NotNull(message = "Please select at least one genre")
    @Size(min = 1, message = "Please select at least one genre")
    private List<@NotNull(message = "Genre id cannot be null") Long> genreIds;
}