package com.damian.photogram.posts.post;

import com.damian.photogram.posts.post.dto.ImageUploadedDTO;
import com.damian.photogram.posts.post.dto.PostDTO;
import com.damian.photogram.posts.post.http.PostCreateRequest;
import jakarta.validation.constraints.NotBlank;
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
    private final PostImageUploaderService postImageUploaderService;

    @Autowired
    public PostController(PostService postService, PostImageUploaderService postImageUploaderService) {
        this.postService = postService;
        this.postImageUploaderService = postImageUploaderService;
    }

    // endpoint to fetch all posts from specific customers
    @GetMapping("/posts/{username}")
    public ResponseEntity<?> getPostsByUsername(
            @PathVariable @NotNull
            String username,
            @PageableDefault(size = 6, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<Post> posts = postService.getPostsByUsername(username, pageable);
        Page<PostDTO> postsDTO = PostDTOMapper.map(posts);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postsDTO);
    }

    // endpoint to add a new post for the logged customers
    @PostMapping("/posts")
    public ResponseEntity<?> addPost(
            @Validated @RequestBody
            PostCreateRequest request
    ) {
        Post post = postService.addPost(request);
        PostDTO postDTO = PostDTOMapper.map(post);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(postDTO);
    }

    // endpoint to delete a post from the logged customers post list.
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
    @GetMapping("/posts/photo/{filename:.+}")
    public ResponseEntity<?> getPostPhoto(
            @PathVariable @NotBlank
            String filename
    ) {
        Resource resource = postImageUploaderService.getImage(filename);
        String contentType = postImageUploaderService.getContentType(resource);

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
        String filename = postImageUploaderService.uploadImage(file);
        ImageUploadedDTO imageUploadedDTO = new ImageUploadedDTO(filename);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(imageUploadedDTO);
    }
}

