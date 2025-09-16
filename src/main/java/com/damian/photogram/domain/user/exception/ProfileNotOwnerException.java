package com.damian.photogram.domain.user.exception;

public class ProfileNotOwnerException extends ProfileException {

    public ProfileNotOwnerException(String message, Long profileId, Long customerId) {
        super(message, profileId, customerId);
    }

}
