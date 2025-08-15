package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.otus.hw.model.migrate.GenreMigrate;
import ru.otus.hw.model.mongo.MongoGenre;
import ru.otus.hw.model.relation.Genre;
import ru.otus.hw.repository.migrate.GenreMigrateRepository;
import ru.otus.hw.repository.relation.GenreRepository;


@RequiredArgsConstructor
@Service
public class GenreMigrateService {

    private final GenreRepository genreRepository;

    private final GenreMigrateRepository genreMigrateRepository;

    @Cacheable(value = "genreCache", key = "#mongoGenre.id")
    public Genre save(MongoGenre mongoGenre) {
        return genreMigrateRepository.findByMongoId(mongoGenre.getId())
                .map(m -> genreRepository.findById(m.getGenreId()).orElseThrow())
                .orElseGet(() -> {
                    Genre genre = new Genre(null, mongoGenre.getName());
                    genre = genreRepository.save(genre);
                    genreMigrateRepository.save(new GenreMigrate(mongoGenre.getId(), genre.getId()));
                    return genre;
                });
    }
}
