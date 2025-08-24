package com.damian.photogram.domain.customer.helper;

public class ProfileHelper {
    private static final String PROFILE_IMAGE_UPLOAD_PATH = "customers/{id}/";

    public static String getProfileImageUploadPath(Long customerId) {
        return PROFILE_IMAGE_UPLOAD_PATH.replace("{id}", customerId.toString());
    }
}
