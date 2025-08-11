package com.damian.photogram.customers.profile;

import com.damian.photogram.common.exception.Exceptions;
import com.damian.photogram.customers.Customer;
import com.damian.photogram.customers.profile.exception.ProfileAuthorizationException;

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
     * Check if the BankingCard belongs to this customers
     *
     * @return ProfileAuthorizationHelper
     */
    public ProfileAuthorizationHelper checkOwner() {
        if (!profile.getOwner().getId().equals(customer.getId())) {
            // banking card does not belong to this customers
            throw new ProfileAuthorizationException(
                    Exceptions.PROFILE.ACCESS_FORBIDDEN
            );
        }
        return this;
    }
}
