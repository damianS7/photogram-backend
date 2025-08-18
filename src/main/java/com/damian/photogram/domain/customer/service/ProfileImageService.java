package com.damian.photogram.domain.customer.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.service.ImageCacheService;
import com.damian.photogram.domain.customer.exception.ProfileNotFoundException;
import com.damian.photogram.domain.customer.exception.ProfilePhotoNotFoundException;
import com.damian.photogram.domain.customer.helper.ProfileHelper;
import com.damian.photogram.domain.customer.model.Profile;
import com.damian.photogram.domain.customer.repository.ProfileRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ProfileImageService {
    private final ImageCacheService imageCacheService;
    private final ProfileRepository profileRepository;

    public ProfileImageService(
            ImageCacheService imageCacheService,
            ProfileRepository profileRepository
    ) {
        this.imageCacheService = imageCacheService;
        this.profileRepository = profileRepository;
    }

    /**
     * It gets the customer profile photo
     */
    public Resource getProfileImage(Long customerId) {
        Profile profile = profileRepository.findByCustomer_Id(customerId).orElseThrow(
                () -> new ProfileNotFoundException(Exceptions.CUSTOMER.NOT_FOUND)
        );

        // check if the customer has a profile photo filename stored in db
        if (profile.getImageFilename() == null) {
            throw new ProfilePhotoNotFoundException(Exceptions.PROFILE.IMAGE.NOT_FOUND);
        }

        return imageCacheService.getImage(
                ProfileHelper.getProfileImageUploadPath(customerId),
                profile.getImageFilename()
        );
    }
}
