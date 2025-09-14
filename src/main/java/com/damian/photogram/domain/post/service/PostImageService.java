package com.damian.photogram.domain.post.service;

import com.damian.photogram.core.common.AuthHelper;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.image.service.ImageProcessingService;
import com.damian.photogram.core.image.service.ImageStorageService;
import com.damian.photogram.core.image.service.ImageUploaderService;
import com.damian.photogram.core.image.service.ImageValidationService;
import com.damian.photogram.domain.user.customer.model.Customer;
import com.damian.photogram.domain.post.exception.PostNotFoundException;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.post.repository.PostRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PostImageService {
    public static final String POST_IMAGE_FOLDER = "posts/"; // profile
    private final ImageUploaderService imageUploaderService;
    private final ImageStorageService imageStorageService;
    private final PostRepository postRepository;
    private final ImageValidationService imageValidationService;
    private final ImageProcessingService imageProcessingService;
    private final long COMPRESS_SIZE_TRIGGER = 250L * 1024; // 250 kb
    private final long MAX_IMAGE_SIZE = 5L * 1024 * 1024; // 2 MB
    private final int MAX_WIDTH = 1920; // 1920px
    private final int MAX_HEIGHT = 1080; // 1080px
    private final String[] ALLOWED_IMAGE_TYPES = {"image/jpg", "image/jpeg", "image/png"};

    public PostImageService(
            ImageUploaderService imageUploaderService,
            ImageStorageService imageStorageService,
            PostRepository postRepository,
            ImageValidationService imageValidationService,
            ImageProcessingService imageProcessingService
    ) {
        this.imageUploaderService = imageUploaderService;
        this.imageStorageService = imageStorageService;
        this.postRepository = postRepository;
        this.imageValidationService = imageValidationService;
        this.imageProcessingService = imageProcessingService;
    }

    /**
     * It uploads an image for the post and returns the filename.
     *
     * @param image MultipartFile
     * @return String
     */
    public String uploadImage(MultipartFile image) {
        final Customer currentCustomer = AuthHelper.getLoggedCustomer();

        // run basic image validations
        imageValidationService.validateImage(
                image,
                MAX_IMAGE_SIZE,
                ALLOWED_IMAGE_TYPES
        );

        // At this point the image is guaranteed to be not null and of an allowed type
        // Image optimizations (resize and compress)
        image = imageProcessingService.optimizeImage(image, MAX_WIDTH, MAX_HEIGHT);

        // saving image
        return imageUploaderService.uploadImage(
                image,
                POST_IMAGE_FOLDER
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
                () -> new PostNotFoundException(Exceptions.POST.NOT_FOUND)
        );

        return imageStorageService.getImage(
                ImageUploaderService.getCustomerUploadFolder(post.getAuthor().getId()) + POST_IMAGE_FOLDER,
                post.getPhotoFilename()
        );
    }
}
