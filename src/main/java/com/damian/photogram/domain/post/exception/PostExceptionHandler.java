package com.damian.photogram.domain.post.exception;

import com.damian.photogram.core.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(1)
@RestControllerAdvice
public class PostExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(PostExceptionHandler.class);

    @ExceptionHandler(PostOwnershipException.class) // 403
    public ResponseEntity<ApiResponse<String>> handlePostOwnership(PostOwnershipException ex) {
        log.warn("Unauthorized operation with post: {} by customer: {}", ex.getPostId(), ex.getCustomerId(), ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(CommentOwnershipException.class) // 403
    public ResponseEntity<ApiResponse<String>> handleCommentOwnership(CommentOwnershipException ex) {
        log.warn("Unauthorized operation with comment: {} by customer: {}", ex.getCommentId(), ex.getCustomerId(), ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(PostNotFoundException.class) // 404
    public ResponseEntity<ApiResponse<String>> handlePostNotFound(PostNotFoundException ex) {
        log.warn("Post: {} not found.", ex.getPostId(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(LikeNotFoundException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleLikeNotFound(LikeNotFoundException ex) {
        log.warn("Post: {} is not liked by customer: {} not found.", ex.getPostId(), ex.getCustomerId(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(CommentNotFoundException.class) // 404
    public ResponseEntity<ApiResponse<String>> handleCommentNotFound(CommentNotFoundException ex) {
        log.warn("Comment: {} not found.", ex.getCommentId(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(PostAlreadyLikedException.class)// Handle conflict (409)
    public ResponseEntity<ApiResponse<String>> handlePostAlreadyLiked(PostAlreadyLikedException ex) {
        log.warn("Post: {} already liked by customer: {}", ex.getPostId(), ex.getCustomerId(), ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT));
    }
}