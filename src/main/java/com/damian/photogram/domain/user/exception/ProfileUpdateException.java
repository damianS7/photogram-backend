package com.damian.photogram.domain.user.exception;

public class ProfileUpdateException extends ProfileException {
    public ProfileUpdateException(String message, Long profileId, Long customerId) {
        super(message, profileId, customerId);
    }
}
