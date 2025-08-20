package com.damian.photogram.domain.customer.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.exception.ImageEmptyFileException;
import com.damian.photogram.core.exception.ImageFileSizeExceededException;
import com.damian.photogram.core.exception.ImageInvalidException;
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

    // validations for file uploaded photos
    private void validatePhotoOrElseThrow(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ImageEmptyFileException(
                    Exceptions.IMAGE.EMPTY_FILE
            );
        }

        if (!file.getContentType().startsWith("image/")) {
            throw new ImageInvalidException(
                    Exceptions.IMAGE.ONLY_IMAGES_ALLOWED
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ImageFileSizeExceededException(
                    Exceptions.PROFILE.IMAGE.FILE_SIZE_LIMIT
            );
        }
    }

    /**
     * It uploads an image and set it as customer profile photo
     */
    public Resource uploadImage(String currentPassword, MultipartFile file) {
        final Customer customerLogged = AuthHelper.getLoggedCustomer();

        // validate password
        AuthHelper.validatePassword(customerLogged, currentPassword);

        // run file validations
        this.validatePhotoOrElseThrow(file);

        // saving file
        String filename = imageUploaderService.uploadImage(
                file,
                ProfileHelper.getProfileImageUploadPath(customerLogged.getId()),
                "avatar"
        );

        // update profile photo in db
        customerLogged.getProfile().setImageFilename(filename);
        profileRepository.save(customerLogged.getProfile());

        return imageStorageService.getImage(
                ProfileHelper.getProfileImageUploadPath(customerLogged.getId()),
                filename
        );
    }

    /**
     * It gets the customer profile photo
     */
    public Resource getProfileImage(Long customerId) {
        Profile profile = profileRepository.findByCustomer_Id(customerId).orElseThrow(
                () -> new ProfileNotFoundException(Exceptions.PROFILE.NOT_FOUND)
        );

        // check if the customer has a profile photo filename stored in db
        if (profile.getImageFilename() == null) {
            throw new ProfilePhotoNotFoundException(Exceptions.PROFILE.IMAGE.NOT_FOUND);
        }

        return imageStorageService.getImage(
                ProfileHelper.getProfileImageUploadPath(customerId),
                profile.getImageFilename()
        );
    }
}
