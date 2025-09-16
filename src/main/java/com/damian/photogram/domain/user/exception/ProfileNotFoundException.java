package com.damian.photogram.domain.user.exception;

public class ProfileNotFoundException extends ProfileException {

    public ProfileNotFoundException(String message, Long profileId, Long customerId) {
        super(message, profileId, customerId);
    }
}
