package com.damian.photogram.service.post;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.util.AuthHelper;
import com.damian.photogram.domain.notification.NotificationType;
import com.damian.photogram.domain.post.exception.CommentNotFoundException;
import com.damian.photogram.domain.post.exception.CommentOwnershipException;
import com.damian.photogram.domain.post.exception.PostNotFoundException;
import com.damian.photogram.domain.post.model.Comment;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.post.repository.CommentRepository;
import com.damian.photogram.domain.post.repository.PostRepository;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.service.notification.NotificationService;
import com.damian.photogram.web.rest.notification.dto.NotificationEvent;
import com.damian.photogram.web.rest.post.dto.request.CommentCreateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * This class handle basic operations such add get/add/delete comments into a post.
 */
@Service
public class CommentService {
    private static final Logger log = LoggerFactory.getLogger(CommentService.class);
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final NotificationService notificationService;

    public CommentService(
            PostRepository postRepository,
            CommentRepository commentRepository,
            NotificationService notificationService
    ) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.notificationService = notificationService;
    }

    /**
     * Get comments from a post
     *
     * @param postId   the id of the post
     * @param pageable the pagination parameters
     * @return a page of comments
     */
    public Page<Comment> getPostComments(Long postId, Pageable pageable) {
        log.debug("Fetching comments from post: {}", postId);

        // check if the post exists
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(Exceptions.POST.NOT_FOUND, postId);
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
        log.debug("customer: {} attempting to post a new comment on post: {}", currentCustomer.getId(), postId);

        // find the post
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException(Exceptions.POST.NOT_FOUND, postId)
        );

        // create the comment
        Comment comment = Comment.create(currentCustomer, post)
                                 .setMessage(request.comment());

        // save the created comment
        Comment savedComment = commentRepository.save(comment);

        log.debug(
                "customer: {} posted a new comment: {} on post: {}",
                currentCustomer.getId(),
                comment.getId(),
                postId
        );
        
        return savedComment;
    }

    /**
     * Delete a comment given the id.
     * Customer must be the owner of the comment.
     *
     * @param commentId the id of the comment to delete
     * @throws CommentNotFoundException  if the comment does not exist
     * @throws CommentOwnershipException if the customer is not the author of the comment
     */
    public void deleteComment(Long commentId) {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();
        log.debug("Customer: {} attempting to delete a comment: {}", currentCustomer.getId(), commentId);

        // find the comment
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new CommentNotFoundException(
                        Exceptions.POST.COMMENT.NOT_FOUND,
                        commentId,
                        null,
                        currentCustomer.getId()
                )
        );

        // check if the customer is the author of the comment.
        if (!comment.isAuthor(currentCustomer)) {
            throw new CommentOwnershipException(
                    Exceptions.POST.COMMENT.NOT_AUTHOR,
                    commentId,
                    comment.getPost().getId(),
                    currentCustomer.getId()
            );
        }

        // delete the comment
        commentRepository.deleteById(commentId);
        log.debug("customer: {} deleted comment: {}", currentCustomer.getId(), commentId);
    }

    /**
     * It generates a notification for the comment.
     * The notification will be received for the post owner.
     *
     * @param comment the posted comment
     */
    public void sendCommentNotification(Comment comment) {
        final String authorUsername = comment.getAuthor().getProfile().getUsername();
        Map<String, Object> metadata = Map.of(
                "postId", comment.getPost().getId(),
                "username", authorUsername
        );

        // create the notification event
        NotificationEvent notification = new NotificationEvent(
                comment.getPost().getAuthor().getId(), // owner of the post will be the recipient
                NotificationType.COMMENT,
                metadata,
                authorUsername + " has comment your post.",
                comment.getCreatedAt().toString()
        );

        // publish the notification
        notificationService.publishNotification(notification);
    }
}
