package com.damian.photogram.domain.user.helper;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.domain.user.exception.ProfileNotOwnerException;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.domain.user.model.Profile;

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
        if (!profile.belongsTo(customer)) {
            // banking card does not belong to this customer
            throw new ProfileNotOwnerException(
                    Exceptions.CUSTOMER.PROFILE.NOT_OWNER, profile.getId(), customer.getId()
            );
        }
        return this;
    }
}
