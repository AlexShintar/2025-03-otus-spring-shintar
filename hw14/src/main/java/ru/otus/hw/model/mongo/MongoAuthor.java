package ru.otus.hw.model.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "authors")
public class MongoAuthor {
    @Id
    private String id;

    private String fullName;

    public MongoAuthor(String fullName) {
        this.fullName = fullName;
    }
}
