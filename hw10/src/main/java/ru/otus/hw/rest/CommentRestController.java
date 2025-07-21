package ru.otus.hw.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public ResponseEntity<List<CommentDto>> getComments(@PathVariable long bookId) {
        return ResponseEntity.ok(commentService.findAllByBookId(bookId));
    }

    @PostMapping("/api/v1/book/{bookId}/comment")
    public ResponseEntity<CommentDto> addComment(@PathVariable long bookId,
                                                 @Valid @RequestBody CommentDto commentDto) {
        CommentDto created = commentService.insert(commentDto.getContent(), bookId);
        return ResponseEntity
                .created(URI.create("/api/v1/book/" + bookId + "/comment/" + created.getId()))
                .body(created);
    }

    @PutMapping("/api/v1/book/{bookId}/comment/{commentId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable long bookId,
                                                    @PathVariable long commentId,
                                                    @Valid @RequestBody CommentDto commentDto) {
        CommentDto updated = commentService.update(commentId, bookId, commentDto.getContent());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/api/v1/book/{bookId}/comment/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable long bookId,
                                              @PathVariable long commentId) {
        commentService.deleteById(commentId, bookId);
        return ResponseEntity.noContent().build();
    }
}
