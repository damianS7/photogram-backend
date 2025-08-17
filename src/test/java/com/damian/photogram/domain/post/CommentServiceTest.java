package com.damian.photogram.domain.post;

import com.damian.photogram.domain.customer.exception.CustomerNotFoundException;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import com.damian.photogram.domain.post.dto.request.CommentCreateRequest;
import com.damian.photogram.domain.post.exception.CommentNotFoundException;
import com.damian.photogram.domain.post.exception.PostNotAuthorException;
import com.damian.photogram.domain.post.exception.PostNotFoundException;
import com.damian.photogram.domain.post.model.Comment;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.post.repository.CommentRepository;
import com.damian.photogram.domain.post.repository.PostRepository;
import com.damian.photogram.domain.post.service.CommentService;
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
public class CommentServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private CommentService commentService;

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
    @DisplayName("Should comment in a post")
    void shouldComment() {
        // given
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Post post = new Post();
        post.setId(1L);
        post.setDescription("Hello world");
        post.setCustomer(loggedCustomer);


        CommentCreateRequest request = new CommentCreateRequest(
                "Hello :)"
        );

        Comment comment = new Comment(loggedCustomer, post);
        comment.setComment(request.comment());

        // when
        when(customerRepository.findById(loggedCustomer.getId())).thenReturn(Optional.of(loggedCustomer));
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment result = commentService.addComment(post.getId(), request);

        // then
        assertThat(result)
                .isNotNull()
                .extracting("comment")
                .isEqualTo(request.comment());
        verify(customerRepository, times(1)).findById(loggedCustomer.getId());
        verify(postRepository, times(1)).findById(post.getId());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should not comment when customer not exists")
    void shouldNotCommentWhenCustomerNotFound() {
        // given
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Post post = new Post();
        post.setId(1L);
        post.setDescription("Hello world");
        post.setCustomer(loggedCustomer);


        CommentCreateRequest request = new CommentCreateRequest(
                "Hello :)"
        );

        Comment comment = new Comment(loggedCustomer, post);
        comment.setComment(request.comment());

        // when
        when(customerRepository.findById(loggedCustomer.getId())).thenReturn(Optional.empty());

        // then
        assertThrows(
                CustomerNotFoundException.class,
                () -> commentService.addComment(post.getId(), request)
        );
        verify(customerRepository, times(1)).findById(loggedCustomer.getId());
    }

    @Test
    @DisplayName("Should not comment when post not exists")
    void shouldNotCommentWhenPostNotExists() {
        // given
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Post post = new Post();
        post.setId(1L);
        post.setDescription("Hello world");
        post.setCustomer(loggedCustomer);


        CommentCreateRequest request = new CommentCreateRequest(
                "Hello :)"
        );

        Comment comment = new Comment(loggedCustomer, post);
        comment.setComment(request.comment());

        // when
        when(customerRepository.findById(loggedCustomer.getId())).thenReturn(Optional.of(loggedCustomer));
        when(postRepository.findById(post.getId())).thenReturn(Optional.empty());

        // then
        assertThrows(
                PostNotFoundException.class,
                () -> commentService.addComment(post.getId(), request)
        );
        verify(customerRepository, times(1)).findById(loggedCustomer.getId());
        verify(postRepository, times(1)).findById(post.getId());
    }

    @Test
    @DisplayName("Should delete a comment")
    void shouldDeleteComment() {
        // given
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Post post = new Post();
        post.setId(1L);
        post.setDescription("Hello world");
        post.setCustomer(loggedCustomer);


        CommentCreateRequest request = new CommentCreateRequest(
                "Hello :)"
        );

        Comment comment = new Comment(loggedCustomer, post);
        comment.setId(5L);
        comment.setComment(request.comment());

        // when
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        commentService.deleteComment(comment.getId());

        // then
        verify(commentRepository, times(1)).findById(comment.getId());
        verify(commentRepository, times(1)).deleteById(comment.getId());
    }

    @Test
    @DisplayName("Should not delete a comment when not exists")
    void shouldNotDeleteCommentWhenNotExists() {
        // given
        Customer loggedCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(loggedCustomer);

        Post post = new Post();
        post.setId(1L);
        post.setDescription("Hello world");
        post.setCustomer(loggedCustomer);


        CommentCreateRequest request = new CommentCreateRequest(
                "Hello :)"
        );

        Comment comment = new Comment(loggedCustomer, post);
        comment.setId(5L);
        comment.setComment(request.comment());

        // when
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.empty());
        assertThrows(
                CommentNotFoundException.class,
                () -> commentService.deleteComment(comment.getId())
        );

        // then
        verify(commentRepository, times(1)).findById(comment.getId());
    }

    @Test
    @DisplayName("Should not delete a comment when logged customer is not author")
    void shouldNotDeleteCommentWhenNotAuthor() {
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

        Post post = new Post(loggedCustomer);
        post.setId(1L);
        post.setDescription("Hello world");


        CommentCreateRequest request = new CommentCreateRequest(
                "Hello :)"
        );

        Comment comment = new Comment(author, post);
        comment.setId(5L);
        comment.setComment(request.comment());

        // when
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        assertThrows(
                PostNotAuthorException.class,
                () -> commentService.deleteComment(comment.getId())
        );

        // then
        verify(commentRepository, times(1)).findById(comment.getId());
    }
}
