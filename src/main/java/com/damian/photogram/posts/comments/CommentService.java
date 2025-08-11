package com.damian.photogram.posts.comments;

import com.damian.photogram.common.exception.Exceptions;
import com.damian.photogram.common.utils.AuthHelper;
import com.damian.photogram.customers.Customer;
import com.damian.photogram.customers.CustomerRepository;
import com.damian.photogram.customers.exception.CustomerNotFoundException;
import com.damian.photogram.posts.comments.exception.CommentNotFoundException;
import com.damian.photogram.posts.comments.http.CommentCreateRequest;
import com.damian.photogram.posts.post.Post;
import com.damian.photogram.posts.post.PostRepository;
import com.damian.photogram.posts.post.exception.PostAuthorizationException;
import com.damian.photogram.posts.post.exception.PostNotFoundException;
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
        // TODO: check other methods and ensure that they implements existsById
        // check if the posts exists
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(Exceptions.POSTS.NOT_FOUND);
        }

        return commentRepository.findAllByPostId(postId, pageable);
    }

    // add a new comment
    public Comment addComment(Long postId, CommentCreateRequest request) {
        Customer loggedCustomer = AuthHelper.getLoggedCustomer();

        // check if the customers we want to add as a post exists.
        Customer customer = customerRepository.findById(loggedCustomer.getId()).orElseThrow(
                () -> new CustomerNotFoundException(Exceptions.CUSTOMER.NOT_FOUND)
        );

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException(Exceptions.POSTS.NOT_FOUND)
        );

        Comment comment = new Comment();
        comment.setCustomer(customer);
        comment.setPost(post);
        comment.setComment(request.comment());

        return commentRepository.save(comment);
    }

    // delete a post from the post list of the logged customers.
    public void deleteComment(Long id) {
        Customer loggedCustomer = AuthHelper.getLoggedCustomer();

        // check if the post exists
        Comment comment = commentRepository.findById(id).orElseThrow(
                () -> new CommentNotFoundException(Exceptions.POSTS.NOT_FOUND)
        );

        // check if the logged customers is the owner of the post.
        if (!loggedCustomer.getId().equals(comment.getCustomer().getId())) {
            throw new PostAuthorizationException(Exceptions.POSTS.ACCESS_FORBIDDEN);
        }

        commentRepository.deleteById(id);
    }
}
