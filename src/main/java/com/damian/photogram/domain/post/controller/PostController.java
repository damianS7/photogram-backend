package com.damian.photogram.domain.post.controller;

import com.damian.photogram.core.service.ImageHelper;
import com.damian.photogram.domain.post.dto.response.ImageUploadedDto;
import com.damian.photogram.domain.post.dto.response.PostCreateRequest;
import com.damian.photogram.domain.post.dto.response.PostDto;
import com.damian.photogram.domain.post.mapper.PostDtoMapper;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.post.service.PostImageService;
import com.damian.photogram.domain.post.service.PostService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api/v1")
@RestController
public class PostController {
    private final PostService postService;
    private final PostImageService postImageService;

    @Autowired
    public PostController(
            PostService postService,
            PostImageService postImageService
    ) {
        this.postService = postService;
        this.postImageService = postImageService;
    }

    // endpoint to fetch all post from specific customer
    @GetMapping("/posts/{username}")
    public ResponseEntity<?> getPostsByUsername(
            @PathVariable @NotNull
            String username,
            @PageableDefault(size = 6, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<Post> posts = postService.getPostsByUsername(username, pageable);
        Page<PostDto> postsDTO = PostDtoMapper.toPostDtoPaginated(posts);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postsDTO);
    }

    // endpoint to add a new post for the logged customer
    @PostMapping("/posts")
    public ResponseEntity<?> createPost(
            @Validated @RequestBody
            PostCreateRequest request
    ) {
        Post post = postService.createPost(request);
        PostDto postDTO = PostDtoMapper.toPostDtoPaginated(post);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(postDTO);
    }

    // endpoint to delete a post from the logged.
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<?> deletePost(
            @PathVariable @NotNull @Positive
            Long id
    ) {
        postService.deletePost(id);

        return ResponseEntity
                .noContent()
                .build();
    }

    // endpoint to get a photo
    @GetMapping("/posts/{postId}/photo")
    public ResponseEntity<?> getPostPhoto(
            @PathVariable @Positive
            Long postId
    ) {
        // FIXME imageService.getImage('post', filename)
        Resource resource = postImageService.getImage(postId);
        String contentType = ImageHelper.getContentType(resource);
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    // endpoint to upload profile photo
    @PostMapping("/posts/photo")
    public ResponseEntity<?> uploadPostPhoto(
            // username ...
            @RequestParam("file") MultipartFile file
    ) {
        String filename = postImageService.uploadImage(file);
        ImageUploadedDto imageUploadedDTO = new ImageUploadedDto(filename);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(imageUploadedDTO);
    }
}

