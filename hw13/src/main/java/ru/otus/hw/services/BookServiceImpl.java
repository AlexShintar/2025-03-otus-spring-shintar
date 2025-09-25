package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.security.AclAwareBookReader;

import java.util.List;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;

    private final BookConverter bookConverter;

    private final AclServiceWrapperService aclServiceWrapperService;

    private final AclAwareBookReader aclAwareBookReader;

    @PreAuthorize("hasPermission(#id, 'ru.otus.hw.models.Book', 'READ')")
    @Transactional(readOnly = true)
    @Override
    public BookDto findById(long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Book with id %d not found", id)
                ));
        return bookConverter.toDto(book);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookDto> findAll() {
        return aclAwareBookReader.findAllBooksSecured()
                .stream()
                .map(bookConverter::toDto)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Override
    public BookDto insert(BookDto bookDto) {
        Book book = bookConverter.toEntity(bookDto);
        Book saved = bookRepository.save(book);
        aclServiceWrapperService.createAcl(saved);
        Sid userSid = new GrantedAuthoritySid("ROLE_USER");
        aclServiceWrapperService.addPermission(saved, userSid, BasePermission.READ);
        return bookConverter.toDto(saved);
    }

    @PreAuthorize("hasPermission(#bookDto.id, 'ru.otus.hw.models.Book', 'WRITE') or hasRole('ADMIN')")
    @Transactional
    @Override
    public BookDto update(BookDto bookDto) {
        Book book = bookRepository.findById(bookDto.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Book with id %d not found", bookDto.getId())
                ));
        Book updated = bookRepository.save(bookConverter.updateEntity(book, bookDto));
        return bookConverter.toDto(updated);
    }

    @PreAuthorize("hasPermission(#id, 'ru.otus.hw.models.Book', 'DELETE') or hasRole('ADMIN')")
    @Transactional
    @Override
    public void deleteById(long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Book with id %d not found", id)
                ));
        aclServiceWrapperService.deleteAcl(book);
        bookRepository.delete(book);
    }
}
