package com.damian.photogram.domain.user.exception;

public class ProfileException extends CustomerException {
    private final Long profileId;

    public ProfileException(String message, Long profileId) {
        this(message, profileId, null);
    }

    public ProfileException(String message, Long profileId, Long customerId) {
        super(message, customerId);
        this.profileId = profileId;
    }

    public Long getProfileId() {
        return profileId;
    }
}
