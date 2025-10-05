package ru.otus.hw.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.models.Book;

@Mapper(componentModel = "spring", uses = {AuthorMapper.class, GenreMapper.class})
public interface BookMapper {

    @Mapping(target = "author", source = "author")
    @Mapping(target = "genres", source = "genres")
    BookDto toDto(Book book);
}
