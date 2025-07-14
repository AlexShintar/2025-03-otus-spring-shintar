package ru.otus.hw.testchangelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import com.mongodb.client.MongoDatabase;
import ru.otus.hw.testutil.TestDataFactory;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Genre;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;

import java.util.List;

@ChangeLog(order = "001")
public class TestDatabaseChangelog {

    @ChangeSet(order = "001", id = "dropDb", author = "ash", runAlways = true)
    public void dropDb(MongoDatabase database) {
        database.drop();
    }

    @ChangeSet(order = "002", id = "initAuthors", author = "ash", runAlways = true)
    public void initAuthors(MongockTemplate template) {
        List<Author> authors = TestDataFactory.authors();
        template.insertAll(authors);
    }

    @ChangeSet(order = "003", id = "initGenres", author = "ash", runAlways = true)
    public void initGenres(MongockTemplate template) {
        List<Genre> genres = TestDataFactory.genres();
        template.insertAll(genres);
    }

    @ChangeSet(order = "004", id = "insertBooks", author = "ash", runAlways = true)
    public void insertBooks(MongockTemplate template) {
        List<Book> books = TestDataFactory.books();
        template.insertAll(books);
    }

    @ChangeSet(order = "005", id = "insertComments", author = "ash", runAlways = true)
    public void insertComments(MongockTemplate template) {
        List<Comment> comments = TestDataFactory.comments();
        template.insertAll(comments);
    }
}
