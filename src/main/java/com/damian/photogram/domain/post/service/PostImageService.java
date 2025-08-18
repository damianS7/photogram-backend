package com.damian.photogram.domain.post.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.service.ImageCacheService;
import com.damian.photogram.domain.post.exception.PostNotFoundException;
import com.damian.photogram.domain.post.helper.PostHelper;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.post.repository.PostRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class PostImageService {
    private final ImageCacheService imageCacheService;
    private final PostRepository postRepository;

    public PostImageService(
            ImageCacheService imageCacheService,
            PostRepository postRepository
    ) {
        this.imageCacheService = imageCacheService;
        this.postRepository = postRepository;
    }

    // returns the profile photo as Resource
    public Resource getPostImage(Long postId) {

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException(Exceptions.POSTS.NOT_FOUND)
        );

        return imageCacheService.getImage(
                PostHelper.getPostsImageUploadPath(post.getCustomer().getId()),
                post.getPhotoFilename()
        );
    }

    // TODO
    public void deleteImage(String filename) {
        
    }
}
