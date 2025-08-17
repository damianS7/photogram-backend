package com.damian.photogram.domain.post.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.utils.AuthHelper;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
public class CommentService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CustomerRepository customerRepository;

    public CommentService(
            PostRepository postRepository,
            CommentRepository commentRepository,
            CustomerRepository customerRepository
    ) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.customerRepository = customerRepository;
    }

    public Page<Comment> getCommentsPageByPostId(Long postId, Pageable pageable) {
        // check if the post exists
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(Exceptions.POSTS.NOT_FOUND);
        }

        return commentRepository.findAllByPostId(postId, pageable);
    }

    // add a new comment
    public Comment addComment(Long postId, CommentCreateRequest request) {
        Customer loggedCustomer = AuthHelper.getLoggedCustomer();

        // check if the customer we want to add as a post exists.
        Customer customer = customerRepository.findById(loggedCustomer.getId()).orElseThrow(
                () -> new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND)
        );

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException(Exceptions.POSTS.NOT_FOUND)
        );

        Comment comment = new Comment(customer, post);
        comment.setComment(request.comment());

        return commentRepository.save(comment);
    }

    // delete a post from the post list of the logged customer.
    public void deleteComment(Long id) {
        Customer loggedCustomer = AuthHelper.getLoggedCustomer();

        // check if the post exists
        Comment comment = commentRepository.findById(id).orElseThrow(
                () -> new CommentNotFoundException(Exceptions.POSTS.NOT_FOUND)
        );

        // check if the logged customer is the author of the post.
        if (!isAuthor(loggedCustomer, comment)) {
            throw new PostNotAuthorException(Exceptions.POSTS.NOT_AUTHOR);
        }

        commentRepository.deleteById(id);
    }

    public boolean isAuthor(Customer customer, Comment comment) {
        // check if the customer is the author of the post.
        return customer.getId().equals(comment.getCustomer().getId());
    }
}
