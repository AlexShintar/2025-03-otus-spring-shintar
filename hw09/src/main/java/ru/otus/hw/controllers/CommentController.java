package ru.otus.hw.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.services.CommentService;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private static final String MODEL_ATTR_COMMENT = "comment";

    private static final String REDIRECT_BOOK = "redirect:/book/%s";

    private final CommentService commentService;

    @PostMapping("/book/{bookId}/comment")
    public String addComment(@PathVariable long bookId,
                             @Valid @ModelAttribute(MODEL_ATTR_COMMENT) CommentDto commentDto,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return REDIRECT_BOOK.formatted(bookId);
        }
        commentService.insert(commentDto.getContent(), bookId);
        return REDIRECT_BOOK.formatted(bookId);
    }

    @PutMapping("/book/{bookId}/comment/{commentId}")
    public String updateComment(@PathVariable long bookId,
                                @PathVariable long commentId,
                                @Valid @ModelAttribute(MODEL_ATTR_COMMENT) CommentDto commentDto,
                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return REDIRECT_BOOK.formatted(bookId);
        }
        commentService.update(commentId, commentDto.getContent());
        return REDIRECT_BOOK.formatted(bookId);
    }


    @DeleteMapping("/book/{bookId}/comment/{commentId}")
    public String deleteComment(@PathVariable long bookId,
                                @PathVariable long commentId) {
        commentService.deleteById(commentId);
        return REDIRECT_BOOK.formatted(bookId);
    }
}
