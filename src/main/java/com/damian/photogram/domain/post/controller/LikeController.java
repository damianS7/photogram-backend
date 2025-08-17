package com.damian.photogram.domain.post.controller;

import com.damian.photogram.domain.post.dto.response.LikeDto;
import com.damian.photogram.domain.post.dto.response.PostLikeDataDto;
import com.damian.photogram.domain.post.mapper.LikeDtoMapper;
import com.damian.photogram.domain.post.model.Like;
import com.damian.photogram.domain.post.service.LikeService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1")
@RestController
public class LikeController {
    private final LikeService likeService;

    @Autowired
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    // endpoint to check if logged customer already likes
    @GetMapping("/posts/{postId}/likes")
    public ResponseEntity<?> getPostLikeData(
            @PathVariable @NotNull @Positive
            Long postId
    ) {
        PostLikeDataDto postLikeData = likeService.getPostLikeData(postId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postLikeData);
    }

    // endpoint to add a new like to a post for the logged customer
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> like(
            @PathVariable @NotNull @Positive
            Long postId
    ) {
        Like like = likeService.like(postId);
        LikeDto likeDto = LikeDtoMapper.toLikeDto(like);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(likeDto);
    }

    // endpoint to unlike(delete) a post for the logged customer.
    @DeleteMapping("/posts/{postId}/unlike")
    public ResponseEntity<?> unlike(
            @PathVariable @NotNull @Positive
            Long postId
    ) {
        likeService.unlike(postId);

        return ResponseEntity
                .noContent()
                .build();
    }
}

