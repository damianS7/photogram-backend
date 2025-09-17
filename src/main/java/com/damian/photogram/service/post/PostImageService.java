package com.damian.photogram.service.post;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.util.AuthHelper;
import com.damian.photogram.domain.post.exception.PostNotFoundException;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.post.repository.PostRepository;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.infrastructure.storage.FileStorageService;
import com.damian.photogram.infrastructure.storage.ImageProcessingService;
import com.damian.photogram.infrastructure.storage.ImageUploaderService;
import com.damian.photogram.infrastructure.storage.ImageValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Paths;

/**
 * This class handle the image upload for the post.
 */
@Service
public class PostImageService {
    public static final String POST_IMAGE_FOLDER = "posts"; // profile
    private static final Logger log = LoggerFactory.getLogger(PostImageService.class);
    private final ImageUploaderService imageUploaderService;
    private final FileStorageService fileStorageService;
    private final PostRepository postRepository;
    private final ImageValidationService imageValidationService;
    private final ImageProcessingService imageProcessingService;
    private final long MAX_IMAGE_SIZE = 5L * 1024 * 1024; // 2 MB
    private final int MAX_WIDTH = 1920; // 1920px
    private final int MAX_HEIGHT = 1080; // 1080px
    private final String[] ALLOWED_IMAGE_TYPES = {"image/jpg", "image/jpeg", "image/png"};

    public PostImageService(
            ImageUploaderService imageUploaderService,
            FileStorageService fileStorageService,
            PostRepository postRepository,
            ImageValidationService imageValidationService,
            ImageProcessingService imageProcessingService
    ) {
        this.imageUploaderService = imageUploaderService;
        this.fileStorageService = fileStorageService;
        this.postRepository = postRepository;
        this.imageValidationService = imageValidationService;
        this.imageProcessingService = imageProcessingService;
    }

    public String getPostImageFolder(Long customerId) {
        return Paths.get(
                ImageUploaderService.getCustomerUploadFolder(customerId),
                POST_IMAGE_FOLDER
        ).toString();
    }

    /**
     * It uploads an image for the post and returns the filename.
     *
     * @param image MultipartFile
     * @return String the name of the uploaded file on the server.
     */
    public File uploadImage(MultipartFile image) {
        final Customer currentCustomer = AuthHelper.getLoggedCustomer();
        log.debug("Customer: {} uploading an image for post. ", currentCustomer.getId());

        // run basic validations for the image to be uploaded
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
        final Customer currentCustomer = AuthHelper.getLoggedCustomer();
        log.debug("Customer: {} getting post: {} image", currentCustomer.getId(), postId);

        // find the post
        final Post post = postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException(Exceptions.POST.NOT_FOUND, postId)
        );

        File file = fileStorageService.getFile(
                getPostImageFolder(post.getAuthor().getId()),
                post.getImageFilename()
        );

        return fileStorageService.createResource(file);
    }

    /**
     * Deletes from storage the image from the given post
     *
     * @param postId the post from where image will be deleted.
     */
    public void deleteImage(Long postId) {
        final Customer currentCustomer = AuthHelper.getLoggedCustomer();
        log.debug("Customer: {} deleting the image from post: {}", currentCustomer.getId(), postId);

        // find the post
        final Post post = postRepository.findById(postId).orElseThrow(
                () -> new PostNotFoundException(Exceptions.POST.NOT_FOUND, postId)
        );

        fileStorageService.deleteFile(
                getPostImageFolder(post.getAuthor().getId()),
                post.getImageFilename()
        );
    }
}
