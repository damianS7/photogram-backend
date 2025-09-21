package com.damian.photogram.service.post.like;

import com.damian.photogram.core.AbstractServiceTest;
import com.damian.photogram.domain.post.exception.LikeNotFoundException;
import com.damian.photogram.domain.post.exception.PostAlreadyLikedException;
import com.damian.photogram.domain.post.exception.PostNotFoundException;
import com.damian.photogram.domain.post.model.Like;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.post.repository.LikeRepository;
import com.damian.photogram.domain.post.repository.PostRepository;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.service.post.LikeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class LikeServiceTest extends AbstractServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private LikeService likeService;

    @Test
    @DisplayName("Should like a post")
    void shouldLikePost() {
        // given
        Customer currentCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(currentCustomer);

        Post post = Post.create(currentCustomer)
                        .setId(1L)
                        .setDescription("Hello world");

        Like like = Like.create(post, currentCustomer);

        // when
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(likeRepository.isPostLikedByCustomer(post.getId(), currentCustomer.getId())).thenReturn(false);
        when(likeRepository.save(any(Like.class))).thenReturn(like);
        Like result = likeService.likePost(post.getId());

        // then
        assertThat(result)
                .isNotNull();
        verify(postRepository, times(1)).findById(post.getId());
        verify(likeRepository, times(1)).save(any(Like.class));
    }

    @Test
    @DisplayName("Should not like when post not found")
    void shouldNotLikePostWhenPostNotFound() {
        // given
        Customer currentCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(currentCustomer);

        Post post = new Post();
        post.setId(1L);

        // when
        when(postRepository.findById(post.getId())).thenReturn(Optional.empty());

        // then
        assertThrows(
                PostNotFoundException.class,
                () -> likeService.likePost(post.getId())
        );

    }

    @Test
    @DisplayName("Should not like when post already liked")
    void shouldNotLikePostWhenPostWhenPostAlreadyLiked() {
        // given
        Customer currentCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(currentCustomer);

        Post post = Post.create(currentCustomer)
                        .setId(1L)
                        .setDescription("Hello world");

        // when
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(likeRepository.isPostLikedByCustomer(post.getId(), currentCustomer.getId())).thenReturn(true);

        // then
        assertThrows(
                PostAlreadyLikedException.class,
                () -> likeService.likePost(post.getId())
        );

    }

    @Test
    @DisplayName("Should unlike a post")
    void shouldUnlike() {
        // given
        Customer currentCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(currentCustomer);

        Post post = Post.create(currentCustomer)
                        .setId(1L)
                        .setDescription("Hello world");

        Like like = Like.create(post, currentCustomer);

        // when
        when(postRepository.existsById(post.getId())).thenReturn(true);
        when(likeRepository.findByPostIdAndCustomerId(
                post.getId(),
                currentCustomer.getId()
        )).thenReturn(Optional.of(like));

        likeService.unlike(post.getId());

        // then
        //        assertThat(result).isNotNull().extracting("totalLikes").isEqualTo(0L);
        verify(likeRepository, times(1)).findByPostIdAndCustomerId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Should not unlike when post not found")
    void shouldNotUnlikeWhenPostNotFound() {
        // given
        Customer currentCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(currentCustomer);

        Post post = Post.create(currentCustomer)
                        .setId(1L)
                        .setDescription("Hello world");

        // when
        when(postRepository.existsById(post.getId())).thenReturn(false);

        // then
        assertThrows(
                PostNotFoundException.class,
                () -> likeService.unlike(post.getId())
        );
    }

    @Test
    @DisplayName("Should not unlike when post not liked")
    void shouldNotUnlikeWhenPostNotLiked() {
        // given
        Customer currentCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(currentCustomer);

        Post post = Post.create(currentCustomer)
                        .setId(1L)
                        .setDescription("Hello world");

        // when
        when(postRepository.existsById(post.getId())).thenReturn(true);
        when(likeRepository.findByPostIdAndCustomerId(
                post.getId(),
                currentCustomer.getId()
        )).thenReturn(Optional.empty());

        // then
        assertThrows(
                LikeNotFoundException.class,
                () -> likeService.unlike(post.getId())
        );
    }
}
