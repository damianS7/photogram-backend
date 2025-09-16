package com.damian.photogram.domain.user.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ProfileUpdateException extends ApplicationException {
    private final Long customerId;
    private final Long profileId;

    public ProfileUpdateException(String message, Long customerId, Long profileId) {
        super(message);
        this.customerId = customerId;
        this.profileId = profileId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getProfileId() {
        return profileId;
    }
}
