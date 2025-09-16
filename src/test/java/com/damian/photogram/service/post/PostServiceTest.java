package com.damian.photogram.service.post;

import com.damian.photogram.core.AbstractServiceTest;
import com.damian.photogram.infrastructure.storage.ImageStorageService;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.web.post.dto.request.PostCreateRequest;
import com.damian.photogram.domain.post.exception.PostNotFoundException;
import com.damian.photogram.domain.post.exception.PostOwnershipException;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class PostServiceTest extends AbstractServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("Should create a post")
    void shouldCreatePost() {
        // given
        Customer currentCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(currentCustomer);

        Post post = Post.create(currentCustomer)
                        .setId(1L)
                        .setPhotoFilename("image.jpg")
                        .setDescription("Hello world");

        PostCreateRequest request = new PostCreateRequest(
                post.getPhotoFilename(),
                post.getDescription()
        );

        // when
        when(postRepository.save(any(Post.class))).thenReturn(post);

        Post result = postService.createPost(request);

        // then
        assertThat(result)
                .isNotNull()
                .extracting("photoFilename", "description")
                .containsExactly(request.photoFilename(), request.description());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("Should delete a post")
    void shouldDeletePost() {
        // given
        Customer currentCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(currentCustomer);

        Post post = Post.create(currentCustomer)
                        .setId(1L)
                        .setPhotoFilename("image.jpg")
                        .setDescription("Hello world");

        // when
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        doNothing().when(imageStorageService).deleteImage(anyString(), anyString());
        postService.deletePost(post.getId());

        // then
        verify(postRepository, times(1)).findById(post.getId());
        verify(postRepository, times(1)).deleteById(post.getId());
    }

    @Test
    @DisplayName("Should not delete a post when not exists")
    void shouldNotDeletePostWhenNotExists() {
        // given
        Customer currentCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(currentCustomer);

        Post post = Post.create(currentCustomer)
                        .setId(1L)
                        .setPhotoFilename("image.jpg")
                        .setDescription("Hello world");

        // when
        when(postRepository.findById(post.getId())).thenReturn(Optional.empty());
        assertThrows(
                PostNotFoundException.class,
                () -> postService.deletePost(post.getId())
        );

        // then
        verify(postRepository, times(1)).findById(post.getId());
    }

    @Test
    @DisplayName("Should not delete a comment when logged customer is not author")
    void shouldNotDeletePostWhenNotAuthor() {
        // given
        Customer currentCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(currentCustomer);

        Customer author = new Customer(
                2L, "customer@test.com",
                passwordEncoder.encode("password")
        );

        Post post = Post.create(author)
                        .setId(1L)
                        .setPhotoFilename("image.jpg")
                        .setDescription("Hello world");

        // when
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        assertThrows(
                PostOwnershipException.class,
                () -> postService.deletePost(post.getId())
        );

        // then
        verify(postRepository, times(1)).findById(post.getId());
    }
}
