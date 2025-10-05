package ru.otus.hw.testutil;

import org.bson.types.ObjectId;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;

public final class TestDataFactory {
    private TestDataFactory() {}

    private static final List<Author> AUTHORS = List.of(
            createAuthor("Author_1"),
            createAuthor("Author_2"),
            createAuthor("Author_3")
    );

    public static List<Author> authors() {
        return AUTHORS;
    }

    private static final List<Genre> GENRES = List.of(
            createGenre("Genre_1"),
            createGenre("Genre_2"),
            createGenre("Genre_3"),
            createGenre("Genre_4"),
            createGenre("Genre_5"),
            createGenre("Genre_6")
    );

    public static List<Genre> genres() {
        return GENRES;
    }

    private static final List<Book> BOOKS = List.of(
            createBook("BookTitle_1", AUTHORS.get(0), Set.of(GENRES.get(0), GENRES.get(1), GENRES.get(2))),
            createBook("BookTitle_2", AUTHORS.get(1), Set.of(GENRES.get(3))),
            createBook("BookTitle_3", AUTHORS.get(2), Set.of(GENRES.get(4), GENRES.get(5)))
    );

    public static List<Book> books() {
        return BOOKS;
    }

    private static final List<Comment> COMMENTS = List.of(
            createComment("Comment_1_for_BookTitle_1", BOOKS.get(0)),
            createComment("Comment_2_for_BookTitle_1", BOOKS.get(0)),
            createComment("Comment_1_for_BookTitle_2", BOOKS.get(1)),
            createComment("Comment_2_for_BookTitle_2", BOOKS.get(1)),
            createComment("Comment_1_for_BookTitle_3", BOOKS.get(2))
    );

    public static List<Comment> comments() {
        return COMMENTS;
    }

    private static Author createAuthor(String name) {
        var a = new Author(name);
        a.setId(new ObjectId().toHexString());
        return a;
    }

    private static Genre createGenre(String name) {
        var g = new Genre(name);
        g.setId(new ObjectId().toHexString());
        return g;
    }

    private static Book createBook(String title, Author author, Set<Genre> genres) {
        var b = new Book(title, author, genres);
        b.setId(new ObjectId().toHexString());
        return b;
    }

    private static Comment createComment(String text, Book book) {
        var c = new Comment(text, book);
        c.setId(new ObjectId().toHexString());
        return c;
    }
}
