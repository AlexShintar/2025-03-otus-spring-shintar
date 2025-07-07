package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookFormDto {
    private Long id;

    @NotBlank(message = "Please provide a title")
    @Size(min = 2, max = 255, message = "Title must be between {min} and {max} characters")
    private String title;

    private Long authorId;

    private List<Long> genreIds;
}
