package ru.otus.hw.testchangelog;

import com.github.cloudyrock.mongock.ChangeLog;
import com.github.cloudyrock.mongock.ChangeSet;
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate;
import com.mongodb.client.MongoDatabase;
import ru.otus.hw.testutil.TestDataFactory;
import ru.otus.hw.model.mongo.MongoAuthor;
import ru.otus.hw.model.mongo.MongoGenre;
import ru.otus.hw.model.mongo.MongoBook;
import ru.otus.hw.model.mongo.MongoComment;

import java.util.List;

@ChangeLog(order = "001")
public class TestDatabaseChangelog {

    @ChangeSet(order = "001", id = "dropDb", author = "ash", runAlways = true)
    public void dropDb(MongoDatabase database) {
        database.drop();
    }

    @ChangeSet(order = "002", id = "initAuthors", author = "ash", runAlways = true)
    public void initAuthors(MongockTemplate template) {
        List<MongoAuthor> authors = TestDataFactory.authors();
        template.insertAll(authors);
    }

    @ChangeSet(order = "003", id = "initGenres", author = "ash", runAlways = true)
    public void initGenres(MongockTemplate template) {
        List<MongoGenre> genres = TestDataFactory.genres();
        template.insertAll(genres);
    }

    @ChangeSet(order = "004", id = "insertBooks", author = "ash", runAlways = true)
    public void insertBooks(MongockTemplate template) {
        List<MongoBook> books = TestDataFactory.books();
        template.insertAll(books);
    }

    @ChangeSet(order = "005", id = "insertComments", author = "ash", runAlways = true)
    public void insertComments(MongockTemplate template) {
        List<MongoComment> comments = TestDataFactory.comments();
        template.insertAll(comments);
    }
}
