package com.damian.photogram.domain.post.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.service.ImageCacheService;
import com.damian.photogram.core.service.ImageUploaderService;
import com.damian.photogram.core.utils.AuthHelper;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.post.exception.PostImageFileSizeException;
import com.damian.photogram.domain.post.exception.PostNotFoundException;
import com.damian.photogram.domain.post.helper.PostHelper;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.post.repository.PostRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PostImageService {
    private final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private final ImageUploaderService imageUploaderService;
    private final ImageCacheService imageCacheService;
    private final PostRepository postRepository;

    public PostImageService(
            ImageUploaderService imageUploaderService,
            ImageCacheService imageCacheService,
            PostRepository postRepository
    ) {
        this.imageUploaderService = imageUploaderService;
        this.imageCacheService = imageCacheService;
        this.postRepository = postRepository;
    }

    // validations for post images
    private void validateImageOrThrow(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new PostImageFileSizeException(
                    Exceptions.POSTS.FILE_SIZE_LIMIT
            );
        }
    }

    /**
     * It uploads an image for the post and returns the filename.
     */
    public String uploadImage(MultipartFile file) {
        final Customer customerLogged = AuthHelper.getLoggedCustomer();

        // run file validations
        this.validateImageOrThrow(file);

        // saving file
        return imageUploaderService.uploadImage(
                file,
                PostHelper.getPostsImageUploadPath(customerLogged.getId())
        );
    }

    // returns the profile photo as Resource
    public Resource getImage(Long postId) {

        Post post = postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException(Exceptions.POSTS.NOT_FOUND)
        );

        return imageCacheService.getImage(
                PostHelper.getPostsImageUploadPath(post.getAuthor().getId()),
                post.getPhotoFilename()
        );
    }
}
