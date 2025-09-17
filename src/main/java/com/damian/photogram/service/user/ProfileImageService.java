package com.damian.photogram.service.user;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.util.AuthHelper;
import com.damian.photogram.domain.user.exception.ProfileImageNotFoundException;
import com.damian.photogram.domain.user.exception.ProfileNotFoundException;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.domain.user.model.Profile;
import com.damian.photogram.domain.user.repository.ProfileRepository;
import com.damian.photogram.infrastructure.storage.ImageProcessingService;
import com.damian.photogram.infrastructure.storage.ImageUploaderService;
import com.damian.photogram.infrastructure.storage.ImageValidationService;
import com.damian.photogram.infrastructure.storage.LocalStorageService;
import com.damian.photogram.infrastructure.storage.exception.ImageTooLargeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Service
public class ProfileImageService {

    public static final String PROFILE_IMAGE_FOLDER = "";
    private static final Logger log = LoggerFactory.getLogger(ProfileImageService.class);
    private final ProfileRepository profileRepository;
    private final ImageUploaderService imageUploaderService;
    private final LocalStorageService localStorageService;
    private final ImageProcessingService imageProcessingService;
    private final ImageValidationService imageValidationService;
    private final long COMPRESS_SIZE_TRIGGER = 250L * 1024; // 250 kb
    private final long MAX_IMAGE_SIZE = 2L * 1024 * 1024; // 2 MB
    private final int MAX_WIDTH = 500; // 500px
    private final int MAX_HEIGHT = 500; // 500px
    private final String[] ALLOWED_IMAGE_TYPES = {"image/jpg", "image/jpeg", "image/png"};

    public ProfileImageService(
            LocalStorageService localStorageService,
            ProfileRepository profileRepository,
            ImageUploaderService imageUploaderService,
            ImageProcessingService imageProcessingService,
            ImageValidationService imageValidationService
    ) {
        this.localStorageService = localStorageService;
        this.profileRepository = profileRepository;
        this.imageUploaderService = imageUploaderService;
        this.imageProcessingService = imageProcessingService;
        this.imageValidationService = imageValidationService;
    }

    /**
     * It uploads an image and set it as customer profile photo
     *
     * @param currentPassword the password of the current customer user
     * @param image           the uploaded image
     * @return image filename
     * @throws ImageTooLargeException if the image size exceeds the limit
     */
    public String uploadProfileImage(String currentPassword, MultipartFile image) {
        final Customer currentCustomer = AuthHelper.getLoggedCustomer();
        log.debug("Uploading customer: {} profile image", currentCustomer.getId());

        // validate password
        AuthHelper.validatePassword(currentCustomer, currentPassword);

        // run basic image validations
        imageValidationService.validateImage(
                image,
                MAX_IMAGE_SIZE,
                ALLOWED_IMAGE_TYPES
        );

        // At this point the image is guaranteed to be not null and of an allowed type
        // Image optimizations (resize and compress)
        image = imageProcessingService.optimizeImage(image, MAX_WIDTH, MAX_HEIGHT);

        // Upload the image
        String filename = imageUploaderService.uploadImage(
                image,
                PROFILE_IMAGE_FOLDER,
                "avatar"
        );

        // update profile photo in db
        currentCustomer.getProfile().setImageFilename(filename);
        profileRepository.save(currentCustomer.getProfile());

        return filename;
    }

    /**
     * It gets the customer profile photo
     *
     * @param customerId the id of the customer to get the photo for
     * @return the customer profile photo resource
     * @throws ProfileNotFoundException      if the customer profile does not exist
     * @throws ProfileImageNotFoundException if the customer profile photo does not exist in the db
     */
    public Resource getProfileImage(Long customerId) {
        log.debug("Getting customer: {} profile image", customerId);
        // find the customer profile
        Profile profile = profileRepository.findByCustomer_Id(customerId).orElseThrow(
                () -> new ProfileNotFoundException(Exceptions.CUSTOMER.PROFILE.NOT_FOUND, null, customerId)
        );

        // check if the customer has a profile photo filename stored in db
        if (profile.getImageFilename() == null) {
            throw new ProfileImageNotFoundException(
                    Exceptions.CUSTOMER.PROFILE.IMAGE.NOT_FOUND,
                    profile.getId(),
                    customerId
            );
        }

        File file = localStorageService.getFile(
                ImageUploaderService.getCustomerUploadFolder(customerId) + PROFILE_IMAGE_FOLDER,
                profile.getImageFilename()
        );
        
        // return the image as resource
        return localStorageService.createResource(file);
    }

    /**
     * It gets the current customer profile photo
     *
     * @return the current customer profile photo resource
     */
    public Resource getProfileImage() {
        final Customer currentCustomer = AuthHelper.getLoggedCustomer();

        return this.getProfileImage(currentCustomer.getId());
    }
}
