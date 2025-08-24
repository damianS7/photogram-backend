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

    /**
     * Get comments from a post
     *
     * @param postId   the ID of the post
     * @param pageable the pagination parameters
     * @return a page of comments
     */
    public Page<Comment> getPostComments(Long postId, Pageable pageable) {
        // check if the post exists
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(Exceptions.POSTS.NOT_FOUND);
        }

        return commentRepository.findAllByPostId(postId, pageable);
    }

    /**
     * Add a new comment to the post
     *
     * @param postId  the ID of the post
     * @param request the comment details
     * @return the created comment
     * @throws PostNotFoundException if the post does not exist
     */
    public Comment addComment(Long postId, CommentCreateRequest request) {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();

        // find the post
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException(Exceptions.POSTS.NOT_FOUND)
        );

        // create the comment
        Comment comment = Comment.create(currentCustomer, post)
                                 .setComment(request.comment());

        // save the created comment
        return commentRepository.save(comment);
    }

    /**
     * Delete a comment given the id.
     * Customer must be the owner of the comment.
     *
     * @param id the id of the comment to delete
     * @throws CommentNotFoundException  if the comment does not exist
     * @throws CommentNotAuthorException if the customer is not the author of the comment
     */
    public void deleteComment(Long id) {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();

        // find the comment
        Comment comment = commentRepository.findById(id).orElseThrow(
                () -> new CommentNotFoundException(Exceptions.POSTS.NOT_FOUND)
        );

        // check if the customer is the author of the comment.
        if (!comment.isAuthor(currentCustomer)) {
            throw new CommentNotAuthorException(Exceptions.COMMENT.NOT_AUTHOR);
        }

        // delete the comment
        commentRepository.deleteById(id);
    }
}
