package com.damian.photogram.domain.user.exception;

public class ProfileImageNotFoundException extends ProfileException {

    public ProfileImageNotFoundException(String message, Long profileId) {
        super(message, profileId);
    }

    public ProfileImageNotFoundException(String message, Long profileId, Long customerId) {
        super(message, profileId, customerId);
    }
}
