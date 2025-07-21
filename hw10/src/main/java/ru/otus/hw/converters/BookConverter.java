package ru.otus.hw.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.GenreService;

import java.util.List;

@RequiredArgsConstructor
@Component
public class BookConverter {
    private final AuthorConverter authorConverter;

    private final GenreConverter genreConverter;

    private final AuthorService authorService;

    private final GenreService genreService;

    public BookDto toDto(Book book) {
        return new BookDto(
                book.getId(),
                book.getTitle(),
                authorConverter.toDto(book.getAuthor()),
                book.getGenres().stream()
                        .map(genreConverter::toDto)
                        .toList()
        );
    }

    public Book toEntity(BookDto dto) {
        return new Book(
                dto.getId(),
                dto.getTitle(),
                authorConverter.toEntity(dto.getAuthor()),
                dto.getGenres().stream()
                        .map(genreConverter::toEntity)
                        .toList()
        );
    }

    public Book updateEntity(Book book, BookDto dto) {
        book.setTitle(dto.getTitle());
        book.setAuthor(authorConverter.toEntity(dto.getAuthor()));
        book.getGenres().clear();
        for (GenreDto gDto : dto.getGenres()) {
            Genre genre = genreConverter.toEntity(gDto);
            book.getGenres().add(genre);
        }
        return book;
    }

    public BookDto fromFormDto(BookUpdateDto form, Long id) {
        AuthorDto authorDto = authorConverter.toDto(
                authorService.findById(form.getAuthorId())
        );
        List<GenreDto> genreDtos = form.getGenreIds().stream()
                .distinct()
                .map(genreService::findById)
                .map(genreConverter::toDto)
                .toList();
        return new BookDto(id, form.getTitle(), authorDto, genreDtos);
    }

    public BookDto fromFormDto(BookCreateDto form) {
        AuthorDto authorDto = authorConverter.toDto(
                authorService.findById(form.getAuthorId())
        );
        List<GenreDto> genreDtos = form.getGenreIds().stream()
                .distinct()
                .map(genreService::findById)
                .map(genreConverter::toDto)
                .toList();
        return new BookDto(null, form.getTitle(), authorDto, genreDtos);
    }
}
