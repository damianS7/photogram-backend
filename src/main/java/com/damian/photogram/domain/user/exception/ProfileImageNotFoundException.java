package com.damian.photogram.domain.user.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ProfileImageNotFoundException extends ApplicationException {
    private final Long profileId;

    public ProfileImageNotFoundException(String message, Long profileId) {
        super(message);
        this.profileId = profileId;
    }

    public Long getProfileId() {
        return profileId;
    }
}
