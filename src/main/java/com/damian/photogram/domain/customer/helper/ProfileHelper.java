package com.damian.photogram.domain.customer.helper;

import com.damian.photogram.core.image.service.ImageUploaderService;

public class ProfileHelper {

    public static String getProfileImageUploadPath(Long customerId) {
        return ImageUploaderService.ROOT_UPLOAD_FOLDER + customerId;
    }
}
