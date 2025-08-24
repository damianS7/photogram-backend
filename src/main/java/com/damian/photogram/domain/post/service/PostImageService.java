package com.damian.photogram.domain.post.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.service.ImageStorageService;
import com.damian.photogram.core.service.ImageUploaderService;
import com.damian.photogram.core.utils.AuthHelper;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.post.exception.PostImageFileSizeExceededException;
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
    private final ImageStorageService imageStorageService;
    private final PostRepository postRepository;

    public PostImageService(
            ImageUploaderService imageUploaderService,
            ImageStorageService imageStorageService,
            PostRepository postRepository
    ) {
        this.imageUploaderService = imageUploaderService;
        this.imageStorageService = imageStorageService;
        this.postRepository = postRepository;
    }

    /**
     * Run specific validations for post images
     *
     * @param file MultipartFile
     * @throws PostImageFileSizeExceededException if the file size exceeds
     */
    private void validateImageOrThrow(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new PostImageFileSizeExceededException(
                    Exceptions.POSTS.IMAGE.FILE_SIZE_LIMIT
            );
        }
    }

    /**
     * It uploads an image for the post and returns the filename.
     *
     * @param image MultipartFile
     * @return String
     */
    public String uploadImage(MultipartFile image) {
        final Customer currentCustomer = AuthHelper.getLoggedCustomer();

        // run image validations
        this.validateImageOrThrow(image);

        // saving image
        return imageUploaderService.uploadImage(
                image,
                PostHelper.getPostsImagePath(currentCustomer.getId())
        );
    }

    /**
     * Returns the post image as Resource from storage
     *
     * @param postId id of the post
     * @return Resource
     * @throws PostNotFoundException if the post does not exist
     */
    public Resource getImage(Long postId) {

        // find the post
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException(Exceptions.POSTS.NOT_FOUND)
        );

        return imageStorageService.getImage(
                PostHelper.getPostsImagePath(post.getAuthor().getId()),
                post.getPhotoFilename()
        );
    }
}
