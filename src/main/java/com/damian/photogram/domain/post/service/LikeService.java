package com.damian.photogram.domain.post.service;

import com.damian.photogram.app.notification.NotificationService;
import com.damian.photogram.app.notification.NotificationType;
import com.damian.photogram.app.notification.dto.NotificationEvent;
import com.damian.photogram.core.common.AuthHelper;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.domain.user.customer.model.Customer;
import com.damian.photogram.domain.post.dto.response.PostLikeDataDto;
import com.damian.photogram.domain.post.exception.LikeNotFoundException;
import com.damian.photogram.domain.post.exception.PostAlreadyLikedException;
import com.damian.photogram.domain.post.exception.PostNotFoundException;
import com.damian.photogram.domain.post.model.Like;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.post.repository.LikeRepository;
import com.damian.photogram.domain.post.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    public LikeService(
            LikeRepository likeRepository,
            PostRepository postRepository,
            NotificationService notificationService
    ) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.notificationService = notificationService;
    }

    /**
     * Get the like data for a specific post.
     *
     * @param postId The ID of the post to retrieve like data for.
     * @return The like data for the specified post.
     * @throws PostNotFoundException If the post does not exist.
     */
    public PostLikeDataDto getPostLikeData(Long postId) {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();

        // check if the post exists
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(Exceptions.POST.NOT_FOUND);
        }

        // get the like data for the specified post
        return new PostLikeDataDto(
                postId,
                likeRepository.isPostLikedByCustomer(postId, currentCustomer.getId()),
                likeRepository.countLikesFromPost(postId)
        );
    }

    /**
     * Like a post.
     * The like will be assigned to the current customer.
     *
     * @param postId The ID of the post to like.
     * @return The like object representing the like.
     * @throws PostNotFoundException     If the post does not exist.
     * @throws PostAlreadyLikedException If the post is already liked by the current customer.
     */
    public Like likePost(Long postId) {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();

        // find the post to like
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException(Exceptions.POST.NOT_FOUND)
        );

        // check if post its already liked by the current customer
        if (likeRepository.isPostLikedByCustomer(postId, currentCustomer.getId())) {
            throw new PostAlreadyLikedException(Exceptions.POST.ALREADY_LIKED);
        }

        // save the like
        return likeRepository.save(
                new Like(post, currentCustomer)
        );
    }

    /**
     * Unlike a post.
     * The unlike will be assigned to the current customer.
     *
     * @param postId The ID of the post to unlike.
     * @throws PostNotFoundException If the post does not exist.
     * @throws LikeNotFoundException If the post like does not exist.
     */
    public void unlike(Long postId) {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();

        // check if the post exists
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(Exceptions.POST.NOT_FOUND);
        }

        // check if the like exists
        Like like = likeRepository
                .findByPostIdAndCustomerId(postId, currentCustomer.getId())
                .orElseThrow(
                        () -> new LikeNotFoundException(Exceptions.POST.LIKE.NOT_FOUND)
                );

        likeRepository.deleteById(like.getId());
    }

    /**
     * It generates a notification for the like.
     *
     * @param like
     */
    public void sendLikeNotification(Like like) {
        final String likerUsername = like.getCustomer().getProfile().getUsername();
        Map<String, Object> metadata = Map.of(
                "postId", like.getPost().getId(),
                "username", likerUsername
        );

        // create the notification event
        NotificationEvent notification = new NotificationEvent(
                like.getPost().getAuthor().getId(),
                NotificationType.LIKE,
                metadata,
                likerUsername + " has liked your post.",
                like.getCreatedAt().toString()
        );

        // publish the notification
        notificationService.publishNotification(notification);
    }
}
