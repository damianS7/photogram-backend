package com.damian.photogram.domain.post.controller;

import com.damian.photogram.domain.post.dto.request.CommentCreateRequest;
import com.damian.photogram.domain.post.dto.response.CommentDto;
import com.damian.photogram.domain.post.mapper.CommentDtoMapper;
import com.damian.photogram.domain.post.model.Comment;
import com.damian.photogram.domain.post.service.CommentService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1")
@RestController
public class CommentController {
    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // endpoint to fetch (paginated) comments from specific post
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<?> getPostComments(
            @PathVariable @NotNull @Positive
            Long postId,
            @PageableDefault(size = 8, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<Comment> comments = commentService.getPostComments(postId, pageable);
        Page<CommentDto> commentsDTO = CommentDtoMapper.map(comments);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(commentsDTO);
    }

    // endpoint to add a new comment for the given post
    @PostMapping("/posts/{postId}/comment")
    public ResponseEntity<?> addComment(
            @PathVariable @NotNull @Positive
            Long postId,
            @Validated @RequestBody
            CommentCreateRequest request
    ) {
        Comment comment = commentService.addComment(postId, request);
        CommentDto commentDto = CommentDtoMapper.map(comment);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(commentDto);
    }

    // endpoint to delete a comment
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> deleteComment(
            @PathVariable @NotNull @Positive
            Long id
    ) {
        commentService.deleteComment(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}

