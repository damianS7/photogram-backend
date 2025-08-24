package com.damian.photogram.domain.customer.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.exception.ImageFileSizeExceededException;
import com.damian.photogram.core.service.ImageStorageService;
import com.damian.photogram.core.service.ImageUploaderService;
import com.damian.photogram.core.utils.AuthHelper;
import com.damian.photogram.domain.customer.exception.ProfileNotFoundException;
import com.damian.photogram.domain.customer.exception.ProfilePhotoNotFoundException;
import com.damian.photogram.domain.customer.helper.ProfileHelper;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.model.Profile;
import com.damian.photogram.domain.customer.repository.ProfileRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfileImageService {
    private final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2 MB
    private final ProfileRepository profileRepository;
    private final ImageUploaderService imageUploaderService;
    private final ImageStorageService imageStorageService;

    public ProfileImageService(
            ImageStorageService imageStorageService,
            ProfileRepository profileRepository,
            ImageUploaderService imageUploaderService
    ) {
        this.imageStorageService = imageStorageService;
        this.profileRepository = profileRepository;
        this.imageUploaderService = imageUploaderService;
    }

    /**
     * Run validations for the uploaded image
     *
     * @param file the uploaded image
     * @throws ImageFileSizeExceededException if the image size exceeds the limit
     */
    private void validateImageOrElseThrow(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ImageFileSizeExceededException(
                    Exceptions.PROFILE.IMAGE.FILE_SIZE_LIMIT
            );
        }
    }

    /**
     * It uploads an image and set it as customer profile photo
     *
     * @param currentPassword the password of the current customer user
     * @param file            the uploaded image
     * @return the uploaded image resource
     * @throws ImageFileSizeExceededException if the image size exceeds the limit
     */
    public String uploadImage(String currentPassword, MultipartFile file) {
        final Customer currentCustomer = AuthHelper.getLoggedCustomer();

        // validate password
        AuthHelper.validatePassword(currentCustomer, currentPassword);

        // run file validations
        this.validateImageOrElseThrow(file);

        // Save the uploaded file and return the stored filename
        String filename = imageUploaderService.uploadImage(
                file,
                ProfileHelper.getProfileImageUploadPath(currentCustomer.getId()),
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
     * @throws ProfilePhotoNotFoundException if the customer profile photo does not exist in the db
     */
    public Resource getProfileImage(Long customerId) {
        // find the customer profile
        Profile profile = profileRepository.findByCustomer_Id(customerId).orElseThrow(
                () -> new ProfileNotFoundException(Exceptions.PROFILE.NOT_FOUND)
        );

        // check if the customer has a profile photo filename stored in db
        if (profile.getImageFilename() == null) {
            throw new ProfilePhotoNotFoundException(Exceptions.PROFILE.IMAGE.NOT_FOUND);
        }

        // return the image as resource
        return imageStorageService.getImage(
                ProfileHelper.getProfileImageUploadPath(customerId),
                profile.getImageFilename()
        );
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
