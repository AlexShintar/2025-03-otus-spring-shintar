package ru.otus.hw.model.mongo;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "comments")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class MongoComment {
    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    private String id;

    private String content;

    @DBRef(lazy = true)
    private MongoBook book;

    public MongoComment(String content, MongoBook book) {
        this.content = content;
        this.book = book;
    }
}
