package ru.otus.hw.testutil;

import org.bson.types.ObjectId;
import ru.otus.hw.model.mongo.MongoAuthor;
import ru.otus.hw.model.mongo.MongoGenre;
import ru.otus.hw.model.mongo.MongoBook;
import ru.otus.hw.model.mongo.MongoComment;

import java.util.List;
import java.util.Set;

public final class TestDataFactory {
    private TestDataFactory() {}

    private static final List<MongoAuthor> AUTHORS = List.of(
            createAuthor("Author_1"),
            createAuthor("Author_2"),
            createAuthor("Author_3")
    );

    public static List<MongoAuthor> authors() {
        return AUTHORS;
    }

    private static final List<MongoGenre> GENRES = List.of(
            createGenre("Genre_1"),
            createGenre("Genre_2"),
            createGenre("Genre_3"),
            createGenre("Genre_4"),
            createGenre("Genre_5"),
            createGenre("Genre_6")
    );

    public static List<MongoGenre> genres() {
        return GENRES;
    }

    private static final List<MongoBook> BOOKS = List.of(
            createBook("BookTitle_1", AUTHORS.get(0), Set.of(GENRES.get(0), GENRES.get(1), GENRES.get(2))),
            createBook("BookTitle_2", AUTHORS.get(1), Set.of(GENRES.get(3))),
            createBook("BookTitle_3", AUTHORS.get(2), Set.of(GENRES.get(4), GENRES.get(5)))
    );

    public static List<MongoBook> books() {
        return BOOKS;
    }

    private static final List<MongoComment> COMMENTS = List.of(
            createComment("Comment_1_for_BookTitle_1", BOOKS.get(0)),
            createComment("Comment_2_for_BookTitle_1", BOOKS.get(0)),
            createComment("Comment_1_for_BookTitle_2", BOOKS.get(1)),
            createComment("Comment_2_for_BookTitle_2", BOOKS.get(1)),
            createComment("Comment_1_for_BookTitle_3", BOOKS.get(2))
    );

    public static List<MongoComment> comments() {
        return COMMENTS;
    }

    private static MongoAuthor createAuthor(String name) {
        var a = new MongoAuthor(name);
        a.setId(new ObjectId().toHexString());
        return a;
    }

    private static MongoGenre createGenre(String name) {
        var g = new MongoGenre(name);
        g.setId(new ObjectId().toHexString());
        return g;
    }

    private static MongoBook createBook(String title, MongoAuthor author, Set<MongoGenre> genres) {
        var b = new MongoBook(title, author, genres);
        b.setId(new ObjectId().toHexString());
        return b;
    }

    private static MongoComment createComment(String text, MongoBook book) {
        var c = new MongoComment(text, book);
        c.setId(new ObjectId().toHexString());
        return c;
    }
}
