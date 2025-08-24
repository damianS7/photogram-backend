package com.damian.photogram.domain.post.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.utils.AuthHelper;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.post.dto.request.CommentCreateRequest;
import com.damian.photogram.domain.post.exception.CommentNotAuthorException;
import com.damian.photogram.domain.post.exception.CommentNotFoundException;
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

    public CommentService(
            PostRepository postRepository,
            CommentRepository commentRepository
    ) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    // get comments by post id
    public Page<Comment> getCommentsPagedByPostId(Long postId, Pageable pageable) {
        // check if the post exists
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(Exceptions.POSTS.NOT_FOUND);
        }

        return commentRepository.findAllByPostId(postId, pageable);
    }

    // add a new comment
    public Comment addComment(Long postId, CommentCreateRequest request) {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();

        // check if the post exists
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException(Exceptions.POSTS.NOT_FOUND)
        );

        Comment comment = new Comment(currentCustomer, post);
        comment.setComment(request.comment());

        return commentRepository.save(comment);
    }

    // delete a comment given the id. Logged customer must be the owner of the comment.
    public void deleteComment(Long id) {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();

        // check if the comment exists
        Comment comment = commentRepository.findById(id).orElseThrow(
                () -> new CommentNotFoundException(Exceptions.POSTS.NOT_FOUND)
        );

        // check if the logged customer is the author of the post.
        if (!comment.isAuthor(currentCustomer)) {
            throw new CommentNotAuthorException(Exceptions.COMMENT.NOT_AUTHOR);
        }

        commentRepository.deleteById(id);
    }
}
