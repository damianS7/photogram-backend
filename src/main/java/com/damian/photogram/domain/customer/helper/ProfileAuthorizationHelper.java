package com.damian.photogram.domain.customer.helper;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.domain.customer.exception.ProfileNotOwnerException;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.model.Profile;

public class ProfileAuthorizationHelper {
    private Customer customer;
    private Profile profile;

    public static ProfileAuthorizationHelper authorize(Customer customer, Profile profile) {
        ProfileAuthorizationHelper helper = new ProfileAuthorizationHelper();
        helper.profile = profile;
        helper.customer = customer;
        return helper;
    }

    /**
     * Check if the BankingCard belongs to this customer
     *
     * @return ProfileAuthorizationHelper
     */
    public ProfileAuthorizationHelper checkOwner() {
        if (!profile.getOwner().getId().equals(customer.getId())) {
            // banking card does not belong to this customer
            throw new ProfileNotOwnerException(
                    Exceptions.PROFILE.NOT_OWNER
            );
        }
        return this;
    }
}
