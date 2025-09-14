package com.damian.photogram.domain.user.customer.exception;

import com.damian.photogram.core.exception.ApplicationException;

public class ProfileNotFoundException extends ApplicationException {
    private final Long profileId;
    private final Long customerId;

    public ProfileNotFoundException(String message, Long profileId, Long customerId) {
        super(message);
        this.profileId = profileId;
        this.customerId = customerId;
    }

    public ProfileNotFoundException(String message) {
        this(message, null, null);
    }

    public Long getProfileId() {
        return profileId;
    }

    public Long getCustomerId() {
        return customerId;
    }
}
