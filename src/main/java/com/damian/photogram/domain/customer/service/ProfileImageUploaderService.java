package com.damian.photogram.domain.customer.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.service.ImageCacheService;
import com.damian.photogram.core.service.ImageUploaderService;
import com.damian.photogram.core.utils.AuthHelper;
import com.damian.photogram.domain.customer.exception.ProfileAuthorizationException;
import com.damian.photogram.domain.customer.helper.ProfileHelper;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.ProfileRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfileImageUploaderService {
    private final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private final ProfileRepository profileRepository;
    private final ImageUploaderService imageUploaderService;
    private final ImageCacheService imageCacheService;

    public ProfileImageUploaderService(
            ProfileRepository profileRepository,
            ImageUploaderService imageUploaderService,
            ImageCacheService imageCacheService
    ) {
        this.profileRepository = profileRepository;
        this.imageUploaderService = imageUploaderService;
        this.imageCacheService = imageCacheService;
    }

    // validations for file uploaded photos
    private void validatePhotoOrElseThrow(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ProfileAuthorizationException(
                    Exceptions.IMAGE.EMPTY_FILE
            );
        }

        if (!file.getContentType().startsWith("image/")) {
            throw new ProfileAuthorizationException(
                    Exceptions.IMAGE.ONLY_IMAGES_ALLOWED
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ProfileAuthorizationException(
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

        return imageCacheService.getImage(
                ProfileHelper.getProfileImageUploadPath(customerLogged.getId()),
                filename
        );
    }
}
