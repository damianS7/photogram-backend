package com.damian.photogram.domain.post;

import com.damian.photogram.core.service.ImageUploaderService;
import com.damian.photogram.domain.customer.exception.CustomerNotFoundException;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import com.damian.photogram.domain.post.dto.response.PostCreateRequest;
import com.damian.photogram.domain.post.exception.PostNotAuthorException;
import com.damian.photogram.domain.post.exception.PostNotFoundException;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.post.repository.CommentRepository;
import com.damian.photogram.domain.post.repository.PostRepository;
import com.damian.photogram.domain.post.service.PostService;
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
public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ImageUploaderService imageUploaderService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private PostService postService;

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
    @DisplayName("Should create a post")
    void shouldCreatePost() {
        // given
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Post post = new Post();
        post.setId(1L);
        post.setPhotoFilename("dfsdfksdfsdf.jpg");
        post.setDescription("Hello world");
        post.setAuthor(loggedCustomer);

        PostCreateRequest request = new PostCreateRequest(
                post.getPhotoFilename(),
                post.getDescription()
        );

        // when
        when(customerRepository.findById(loggedCustomer.getId())).thenReturn(Optional.of(loggedCustomer));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        Post result = postService.createPost(request);

        // then
        assertThat(result)
                .isNotNull()
                .extracting("photoFilename", "description")
                .containsExactly(request.photoFilename(), request.description());
        verify(customerRepository, times(1)).findById(loggedCustomer.getId());
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("Should create a post when customer not found")
    void shouldNotCreatePostWhenCustomerNotFound() {
        // given
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Post post = new Post();
        post.setId(1L);
        post.setPhotoFilename("dfsdfksdfsdf.jpg");
        post.setDescription("Hello world");
        post.setAuthor(loggedCustomer);

        PostCreateRequest request = new PostCreateRequest(
                post.getPhotoFilename(),
                post.getDescription()
        );

        // when
        when(customerRepository.findById(loggedCustomer.getId())).thenReturn(Optional.empty());

        // then
        assertThrows(
                CustomerNotFoundException.class,
                () -> postService.createPost(request)
        );
        verify(customerRepository, times(1)).findById(loggedCustomer.getId());
    }

    @Test
    @DisplayName("Should delete a post")
    void shouldDeletePost() {
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
        postService.deletePost(post.getId());

        // then
        verify(postRepository, times(1)).findById(post.getId());
        verify(postRepository, times(1)).deleteById(post.getId());
    }

    @Test
    @DisplayName("Should not delete a post when not exists")
    void shouldNotDeletePostWhenNotExists() {
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
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Customer author = new Customer(
                2L, "customer@test.com",
                passwordEncoder.encode("password")
        );

        Post post = new Post(author);
        post.setId(1L);
        post.setDescription("Hello world");


        // when
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        assertThrows(
                PostNotAuthorException.class,
                () -> postService.deletePost(post.getId())
        );

        // then
        verify(postRepository, times(1)).findById(post.getId());
    }
}
