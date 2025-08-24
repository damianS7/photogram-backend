package com.damian.photogram.domain.post;

import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.post.exception.LikeNotFoundException;
import com.damian.photogram.domain.post.exception.PostAlreadyLikedException;
import com.damian.photogram.domain.post.exception.PostNotFoundException;
import com.damian.photogram.domain.post.model.Like;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.post.repository.LikeRepository;
import com.damian.photogram.domain.post.repository.PostRepository;
import com.damian.photogram.domain.post.service.LikeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LikeServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private LikeService likeService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    void setUpContext(Customer customer) {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(customer);
    }

    @Test
    @DisplayName("Should like a post")
    void shouldLike() {
        // given
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Post post = new Post();
        post.setId(1L);
        post.setDescription("Hello world");
        post.setAuthor(loggedCustomer);

        Like like = new Like(post, loggedCustomer);

        // when
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(likeRepository.isPostLikedByCustomer(post.getId(), loggedCustomer.getId())).thenReturn(false);
        when(likeRepository.save(any(Like.class))).thenReturn(like);
        Like result = likeService.like(post.getId());

        // then
        assertThat(result)
                .isNotNull();
        verify(postRepository, times(1)).findById(post.getId());
        verify(likeRepository, times(1)).save(any(Like.class));
    }

    @Test
    @DisplayName("Should not like when post not found")
    void shouldNotLikeWhenPostNotFound() {
        // given
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Post post = new Post();
        post.setId(1L);

        // when
        when(postRepository.findById(post.getId())).thenReturn(Optional.empty());

        // then
        assertThrows(
                PostNotFoundException.class,
                () -> likeService.like(post.getId())
        );

    }

    @Test
    @DisplayName("Should not like when post already liked")
    void shouldNotLikeWhenPostWhenPostAlreadyLiked() {
        // given
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Post post = new Post();
        post.setId(1L);
        post.setDescription("Hello world");
        post.setAuthor(loggedCustomer);

        // when
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(likeRepository.isPostLikedByCustomer(post.getId(), loggedCustomer.getId())).thenReturn(true);

        // then
        assertThrows(
                PostAlreadyLikedException.class,
                () -> likeService.like(post.getId())
        );

    }

    @Test
    @DisplayName("Should unlike a post")
    void shouldUnlike() {
        // given
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Post post = new Post();
        post.setId(1L);
        post.setDescription("Hello world");
        post.setAuthor(loggedCustomer);

        Like like = new Like(post, loggedCustomer);

        // when
        when(postRepository.existsById(post.getId())).thenReturn(true);
        when(likeRepository.findByPostIdAndCustomerId(
                post.getId(),
                loggedCustomer.getId()
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
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Post post = new Post();
        post.setId(1L);
        post.setDescription("Hello world");
        post.setAuthor(loggedCustomer);

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
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Post post = new Post();
        post.setId(1L);
        post.setDescription("Hello world");
        post.setAuthor(loggedCustomer);

        // when
        when(postRepository.existsById(post.getId())).thenReturn(true);
        when(likeRepository.findByPostIdAndCustomerId(
                post.getId(),
                loggedCustomer.getId()
        )).thenReturn(Optional.empty());

        // then
        assertThrows(
                LikeNotFoundException.class,
                () -> likeService.unlike(post.getId())
        );
    }
}
