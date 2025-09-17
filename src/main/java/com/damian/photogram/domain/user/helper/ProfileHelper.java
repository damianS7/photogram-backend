package com.damian.photogram.domain.user.helper;

import com.damian.photogram.infrastructure.storage.ImageUploaderService;

public class ProfileHelper {

    public static String getProfileImageUploadPath(Long customerId) {
        return ImageUploaderService.UPLOAD_PATH + customerId;
    }
}
