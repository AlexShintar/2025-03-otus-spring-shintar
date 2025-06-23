package ru.otus.hw.repositories;


import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.FETCH;

@Repository
@Transactional
public class JpaBookRepository implements BookRepository {

    private static final String GRAPH_NAME = "book:author-genre-entity-graph";

    @PersistenceContext
    private EntityManager em;


    public Optional<Book> findById(long id) {
        String jpql = "SELECT b FROM Book b WHERE b.id = :id";
        TypedQuery<Book> query = em.createQuery(jpql, Book.class);

        EntityGraph<?> graph = em.getEntityGraph(GRAPH_NAME);
        query.setHint(FETCH.getKey(), graph);

        query.setParameter("id", id);
        return query.getResultList().stream().findFirst();
    }


    @Override
    public List<Book> findAll() {
        String jpql = "SELECT b FROM Book b ORDER BY b.id";
        TypedQuery<Book> query = em.createQuery(jpql, Book.class);

        EntityGraph<?> graph = em.getEntityGraph(GRAPH_NAME);
        query.setHint(FETCH.getKey(), graph);

        return query.getResultList();
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            em.persist(book);
            return book;
        } else {
            return em.merge(book);
        }
    }

    @Override
    public void deleteById(long id) {
        Book book = em.find(Book.class, id);
        if (book != null) {
            em.remove(book);
        }
    }
}
