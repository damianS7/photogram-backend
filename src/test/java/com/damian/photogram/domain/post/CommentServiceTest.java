package com.damian.photogram.domain.post;

import com.damian.photogram.AbstractServiceTest;
import com.damian.photogram.domain.user.customer.model.Customer;
import com.damian.photogram.domain.post.dto.request.CommentCreateRequest;
import com.damian.photogram.domain.post.exception.CommentNotFoundException;
import com.damian.photogram.domain.post.exception.CommentOwnershipException;
import com.damian.photogram.domain.post.exception.PostNotFoundException;
import com.damian.photogram.domain.post.model.Comment;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.post.repository.CommentRepository;
import com.damian.photogram.domain.post.repository.PostRepository;
import com.damian.photogram.domain.post.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommentServiceTest extends AbstractServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("Should get comments paginated")
    void shouldGetCommentsPaginated() {
        // given
        Customer currentCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        //        setUpContext(currentCustomer);

        Post post = Post.create(currentCustomer)
                        .setId(1L)
                        .setPhotoFilename("image.jpg")
                        .setDescription("Hello world");

        Comment comment1 = Comment.create(currentCustomer, post)
                                  .setComment("comment 1");

        Comment comment2 = Comment.create(currentCustomer, post)
                                  .setComment("comment 2");

        Set<Comment> commentList = Set.of(
                comment1, comment2
        );

        Page<Comment> commentPage = new PageImpl<>(commentList.stream().toList());
        Pageable pageable = PageRequest.of(0, 2);

        // when
        when(postRepository.existsById(post.getId())).thenReturn(true);
        when(commentRepository.findAllByPostId(post.getId(), pageable))
                .thenReturn(commentPage);
        Page<Comment> result = commentService.getPostComments(post.getId(), pageable);

        // then
        assertNotNull(result);
        assertEquals(commentList.size(), result.getSize());
        verify(commentRepository, times(1)).findAllByPostId(post.getId(), pageable);
    }

    @Test
    @DisplayName("Should comment in a post")
    void shouldComment() {
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

        CommentCreateRequest request = new CommentCreateRequest(
                "Hello :)"
        );

        Comment comment = Comment.create(currentCustomer, post)
                                 .setComment(request.comment());

        // when
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment result = commentService.addComment(post.getId(), request);

        // then
        assertThat(result)
                .isNotNull()
                .extracting("comment")
                .isEqualTo(request.comment());
        verify(postRepository, times(1)).findById(post.getId());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should not comment when post not exists")
    void shouldNotCommentWhenPostNotExists() {
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

        CommentCreateRequest request = new CommentCreateRequest(
                "Hello :)"
        );

        // when
        when(postRepository.findById(post.getId())).thenReturn(Optional.empty());

        // then
        assertThrows(
                PostNotFoundException.class,
                () -> commentService.addComment(post.getId(), request)
        );
        verify(postRepository, times(1)).findById(post.getId());
    }

    @Test
    @DisplayName("Should delete a comment")
    void shouldDeleteComment() {
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

        CommentCreateRequest request = new CommentCreateRequest(
                "Hello :)"
        );

        Comment comment = Comment.create(currentCustomer, post)
                                 .setId(5L)
                                 .setComment(request.comment());

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
        Customer currentCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(currentCustomer);

        Post post = Post.create(currentCustomer)
                        .setId(1L)
                        .setPhotoFilename("image.jpg")
                        .setDescription("Hello world");


        CommentCreateRequest request = new CommentCreateRequest(
                "Hello :)"
        );

        Comment comment = Comment.create(currentCustomer, post)
                                 .setId(5L)
                                 .setComment(request.comment());

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
        Customer currentCustomer = new Customer(
                1L, "customer@test.com",
                passwordEncoder.encode("password")
        );
        setUpContext(currentCustomer);

        Customer author = new Customer(
                2L, "customer@test.com",
                passwordEncoder.encode("password")
        );

        Post post = Post.create(currentCustomer)
                        .setId(1L)
                        .setPhotoFilename("image.jpg")
                        .setDescription("Hello world");


        CommentCreateRequest request = new CommentCreateRequest(
                "Hello :)"
        );

        Comment comment = Comment.create(author, post)
                                 .setId(5L)
                                 .setComment(request.comment());

        // when
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.of(comment));
        assertThrows(
                CommentOwnershipException.class,
                () -> commentService.deleteComment(comment.getId())
        );

        // then
        verify(commentRepository, times(1)).findById(comment.getId());
    }
}
