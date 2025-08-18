package com.damian.photogram.domain.customer.helper;

import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.model.Profile;

public class ProfileHelper {
    private static final String PROFILE_IMAGE_UPLOAD_PATH = "customers/{id}/";

    public static boolean isAuthor(Customer customer, Profile profile) {
        // check if the customer is the author of the profile.
        return customer.getId().equals(profile.getCustomerId());
    }

    public static String getProfileImageUploadPath(Long customerId) {
        return PROFILE_IMAGE_UPLOAD_PATH.replace("{id}", customerId.toString());
    }
}
