package ru.otus.hw.changelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import com.mongodb.client.MongoDatabase;
import ru.otus.hw.model.mongo.MongoAuthor;
import ru.otus.hw.model.mongo.MongoBook;
import ru.otus.hw.model.mongo.MongoComment;
import ru.otus.hw.model.mongo.MongoGenre;

import java.util.List;
import java.util.Set;

@ChangeLog(order = "001")
public class DatabaseChangelog {

    private List<MongoAuthor> authors;

    private List<MongoGenre> genres;

    private List<MongoBook> books;

    @ChangeSet(order = "001", id = "dropDb", author = "ash", runAlways = true)
    public void dropDb(MongoDatabase database) {
        database.drop();
    }

    @ChangeSet(order = "002", id = "initAuthors", author = "ash", runAlways = true)
    public void initAuthors(MongockTemplate template) {
        authors = List.of(
                new MongoAuthor("Author_1"),
                new MongoAuthor("Author_2"),
                new MongoAuthor("Author_3")
        );
        template.insertAll(authors);
    }

    @ChangeSet(order = "003", id = "initGenres", author = "ash", runAlways = true)
    public void initGenres(MongockTemplate template) {
        genres = List.of(
                new MongoGenre("Genre_1"),
                new MongoGenre("Genre_2"),
                new MongoGenre("Genre_3"),
                new MongoGenre("Genre_4"),
                new MongoGenre("Genre_5"),
                new MongoGenre("Genre_6")
        );
        template.insertAll(genres);
    }

    @ChangeSet(order = "004", id = "insertBooks", author = "ash", runAlways = true)
    public void insertBooks(MongockTemplate template) {
        books = List.of(
                new MongoBook("BookTitle_1", authors.get(0), Set.of(genres.get(0), genres.get(1), genres.get(2))),
                new MongoBook("BookTitle_2", authors.get(1), Set.of(genres.get(3))),
                new MongoBook("BookTitle_3", authors.get(2), Set.of(genres.get(4), genres.get(5)))
        );
        template.insertAll(books);
    }

    @ChangeSet(order = "005", id = "insertComments", author = "ash", runAlways = true)
    public void insertComments(MongockTemplate template) {
        List<MongoComment> comments = List.of(
                new MongoComment("Comment_1_for_BookTitle_1", books.get(0)),
                new MongoComment("Comment_2_for_BookTitle_1", books.get(0)),
                new MongoComment("Comment_1_for_BookTitle_2", books.get(1)),
                new MongoComment("Comment_2_for_BookTitle_2", books.get(1)),
                new MongoComment("Comment_1_for_BookTitle_3", books.get(2))
        );
        template.insertAll(comments);
    }
}
