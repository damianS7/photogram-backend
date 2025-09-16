package com.damian.photogram.web.post;

import com.damian.photogram.domain.post.model.Like;
import com.damian.photogram.service.post.LikeService;
import com.damian.photogram.web.post.dto.mapper.LikeDtoMapper;
import com.damian.photogram.web.post.dto.response.LikeDto;
import com.damian.photogram.web.post.dto.response.PostLikeDataDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1")
@RestController
public class LikeController {
    private static final Logger log = LoggerFactory.getLogger(LikeController.class);
    private final LikeService likeService;

    @Autowired
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    // endpoint to fetch the like data for a specific post.
    @GetMapping("/posts/{postId}/likes")
    public ResponseEntity<?> getPostLikeData(
            @PathVariable @NotNull @Positive
            Long postId
    ) {
        log.debug("Received request to get like data from post: {}", postId);
        PostLikeDataDto postLikeData = likeService.getPostLikeData(postId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postLikeData);
    }

    // endpoint to add a new like to a post
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> like(
            @PathVariable @NotNull @Positive
            Long postId
    ) {
        log.debug("Received request to like a post: {}", postId);
        Like like = likeService.likePost(postId);
        likeService.sendLikeNotification(like);
        LikeDto likeDto = LikeDtoMapper.toLikeDto(like);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(likeDto);
    }

    // endpoint to unlike a post.
    @DeleteMapping("/posts/{postId}/unlike")
    public ResponseEntity<?> unlike(
            @PathVariable @NotNull @Positive
            Long postId
    ) {
        log.debug("Received request to unlike a post: {}", postId);
        likeService.unlike(postId);

        return ResponseEntity
                .noContent()
                .build();
    }
}

