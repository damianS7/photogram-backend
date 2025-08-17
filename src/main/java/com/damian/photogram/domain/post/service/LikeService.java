package com.damian.photogram.domain.post.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.utils.AuthHelper;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.post.dto.response.PostLikeDataDto;
import com.damian.photogram.domain.post.exception.LikeNotFoundException;
import com.damian.photogram.domain.post.exception.PostAlreadyLikedException;
import com.damian.photogram.domain.post.exception.PostNotFoundException;
import com.damian.photogram.domain.post.model.Like;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.post.repository.LikeRepository;
import com.damian.photogram.domain.post.repository.PostRepository;
import org.springframework.stereotype.Service;


@Service
public class LikeService {
    private final LikeRepository likeRepository;
    private final PostRepository postRepository;

    public LikeService(
            LikeRepository likeRepository,
            PostRepository postRepository
    ) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
    }

    // get the like entity for the current customer and the given post by id.
    public PostLikeDataDto getPostLikeData(Long postId) {
        Customer customer = AuthHelper.getLoggedCustomer();

        // check if the post exists
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(Exceptions.POSTS.NOT_FOUND);
        }

        return new PostLikeDataDto(
                postId,
                likeRepository.isPostLikedByCustomer(postId, customer.getId()),
                likeRepository.countLikesFromPost(postId)
        );
    }

    // logged customer will like the given post by id.
    public Like like(Long postId) {
        Customer customer = AuthHelper.getLoggedCustomer();

        // check if the post exists
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException(Exceptions.POSTS.NOT_FOUND)
        );

        // if post its already liked
        if (likeRepository.isPostLikedByCustomer(postId, customer.getId())) {
            throw new PostAlreadyLikedException(Exceptions.POSTS.ALREADY_LIKED);
        }

        return likeRepository.save(
                new Like(post, customer)
        );
    }

    // unlike customer following by logged customer
    public PostLikeDataDto unlike(Long postId) {
        Customer customer = AuthHelper.getLoggedCustomer();

        // check if the post exists
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(Exceptions.POSTS.NOT_FOUND);
        }

        // check if the like exists
        Like like = likeRepository
                .findByPostIdAndCustomerId(postId, customer.getId())
                .orElseThrow(
                        () -> new LikeNotFoundException(Exceptions.LIKES.NOT_FOUND)
                );

        likeRepository.deleteById(like.getId());
        return getPostLikeData(postId);
    }
}
