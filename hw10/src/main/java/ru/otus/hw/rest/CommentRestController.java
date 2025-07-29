package ru.otus.hw.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.services.CommentService;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentRestController {

    private final CommentService commentService;

    @GetMapping("/api/v1/book/{bookId}/comment")
    public List<CommentDto> getComments(@PathVariable long bookId) {
        return commentService.findAllByBookId(bookId);
    }

    @PostMapping("/api/v1/book/{bookId}/comment")
    public ResponseEntity<CommentDto> addComment(@PathVariable long bookId,
                                                 @Valid @RequestBody CommentDto commentDto) {
        CommentDto created = commentService.insert(commentDto.content(), bookId);
        return ResponseEntity
                .created(URI.create("/api/v1/book/" + bookId + "/comment/" + created.id()))
                .body(created);
    }

    @PutMapping("/api/v1/book/{bookId}/comment/{commentId}")
    public CommentDto updateComment(@PathVariable long bookId,
                                    @PathVariable long commentId,
                                    @Valid @RequestBody CommentDto commentDto) {
        return commentService.update(commentId, bookId, commentDto.content());
    }

    @DeleteMapping("/api/v1/book/{bookId}/comment/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable long bookId,
                              @PathVariable long commentId) {
        commentService.deleteById(commentId, bookId);
    }
}
