package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BookController {

    private static final String REDIRECT_ROOT = "redirect:/";

    private static final String MODEL_ATTR_BOOK = "book";

    private static final String MODEL_ATTR_BOOKS = "bookList";

    private static final String MODEL_ATTR_AUTHORS = "authors";

    private static final String MODEL_ATTR_GENRES = "genres";

    private static final String MODEL_ATTR_COMMENTS = "comments";

    private static final String VIEW_LIST = "list";

    private static final String VIEW_FORM = "edit";

    private static final String VIEW_DETAIL = "detail";

    private final AuthorService authorService;

    private final BookService bookService;

    private final GenreService genreService;

    private final CommentService commentService;

    private final BookConverter bookConverter;

    @ModelAttribute(MODEL_ATTR_AUTHORS)
    public List<AuthorDto> populateAuthors() {
        return authorService.findAll();
    }

    @ModelAttribute(MODEL_ATTR_GENRES)
    public List<GenreDto> populateGenres() {
        return genreService.findAll();
    }

    @GetMapping("/")
    public String listBooks(Model model) {
        model.addAttribute(MODEL_ATTR_BOOKS, bookService.findAll());
        return VIEW_LIST;
    }

    @GetMapping("/book/{id}")
    public String showBook(@PathVariable long id, Model model) {
        model.addAttribute(MODEL_ATTR_BOOK, bookService.findById(id));
        model.addAttribute(MODEL_ATTR_COMMENTS, commentService.findAllByBookId(id));
        return VIEW_DETAIL;
    }

    @GetMapping("/book/new")
    public String newBookForm(Model model) {
        model.addAttribute(MODEL_ATTR_BOOK, new BookFormDto());
        return VIEW_FORM;
    }

    @GetMapping("/book/{id}/edit")
    public String editBookForm(@PathVariable long id, Model model) {
        model.addAttribute(MODEL_ATTR_BOOK, bookConverter.toFormDto(bookService.findById(id)));
        return VIEW_FORM;
    }

    @PostMapping("/book")
    public String createBook(@Valid @ModelAttribute(MODEL_ATTR_BOOK) BookFormDto form,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VIEW_FORM;
        }
        bookService.insert(bookConverter.fromFormDto(form, null));
        return REDIRECT_ROOT;
    }

    @PutMapping("/book/{id}")
    public String updateBook(@PathVariable long id,
                             @Valid @ModelAttribute(MODEL_ATTR_BOOK) BookFormDto form,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return VIEW_FORM;
        }
        bookService.update(bookConverter.fromFormDto(form, id));
        return REDIRECT_ROOT;
    }

    @DeleteMapping("/book/{id}")
    public String deleteBook(@PathVariable long id) {
        bookService.deleteById(id);
        return REDIRECT_ROOT;
    }
}
