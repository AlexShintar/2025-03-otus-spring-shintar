package ru.otus.hw.model.mongo;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "books")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class MongoBook {
    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    private String id;

    private String title;

    private MongoAuthor author;

    private Set<MongoGenre> genres;

    public MongoBook(String title, MongoAuthor author, Set<MongoGenre> genres) {
        this.title = title;
        this.author = author;
        this.genres = genres;
    }
}
