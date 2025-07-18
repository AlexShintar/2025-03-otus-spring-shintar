package ru.otus.hw.repositories;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Author;


import java.util.List;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий на основе JPA для работы с авторами")
@DataJpaTest
class JpaAuthorRepositoryTest {

    @Autowired
    private AuthorRepository repository;

    @Autowired
    private TestEntityManager em;

    @DisplayName("должен загружать автора по id")
    @ParameterizedTest
    @MethodSource("getDbAuthors")
    void shouldReturnCorrectAuthorById(Author template) {
        Author expectedAuthor = em.find(Author.class, template.getId());
        var actualAuthor = repository.findById(template.getId());
        assertThat(actualAuthor).isPresent()
                .get()
                .isEqualTo(expectedAuthor);
    }

    @DisplayName("должен загружать список всех авторов")
    @Test
    void shouldReturnCorrectAuthorsList() {
        List<Author> expectedAuthors = getDbAuthors().stream()
                .map(a -> em.find(Author.class, a.getId()))
                .toList();

        var actualAuthors = repository.findAll();
        assertThat(actualAuthors).containsExactlyElementsOf(expectedAuthors);
    }

    private static List<Author> getDbAuthors() {
        return LongStream.range(1, 4).boxed()
                .map(id -> new Author(id , "Author_" + id))
                .toList();
    }
}
