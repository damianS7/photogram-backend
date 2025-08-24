package com.damian.photogram.domain.customer.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.utils.AuthHelper;
import com.damian.photogram.domain.customer.dto.request.ProfileUpdateRequest;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.exception.ProfileNotFoundException;
import com.damian.photogram.domain.customer.exception.ProfileUpdateValidationException;
import com.damian.photogram.domain.customer.helper.ProfileAuthorizationHelper;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.model.Profile;
import com.damian.photogram.domain.customer.repository.ProfileRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;

@Service
public class ProfileService {
    private final ProfileRepository profileRepository;

    public ProfileService(
            ProfileRepository profileRepository
    ) {
        this.profileRepository = profileRepository;
    }

    /**
     * Get the profile for the current customer
     *
     * @return Profile the profile
     * @throws ProfileNotFoundException if the profile is not found
     */
    public Profile getProfile() {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();

        return this.getProfile(currentCustomer.getId());
    }

    /**
     * Get a profile by id
     *
     * @param profileId the profile id
     * @return Profile the profile
     * @throws ProfileNotFoundException if the profile is not found
     */
    public Profile getProfile(Long profileId) {
        return profileRepository
                .findById(profileId)
                .orElseThrow(
                        () -> new ProfileNotFoundException(Exceptions.PROFILE.NOT_FOUND)
                );
    }

    /**
     * It updates the current customer profile
     *
     * @param request the request containing the updated profile information
     * @return Profile the updated profile
     */
    public Profile updateProfile(ProfileUpdateRequest request) {
        final Customer currentCustomer = AuthHelper.getLoggedCustomer();

        return this.updateProfile(currentCustomer.getProfile().getId(), request);
    }

    /**
     * It updates the customer profile by id
     *
     * @param profileId the id of the profile to be updated
     * @param request   the request containing the updated profile information
     * @return Profile the updated profile
     * @throws ProfileNotFoundException if the profile is not found
     */
    public Profile updateProfile(Long profileId, ProfileUpdateRequest request) {
        final Customer currentCustomer = AuthHelper.getLoggedCustomer();

        // find the profile we want to modify
        Profile profile = profileRepository
                .findById(profileId)
                .orElseThrow(() -> new ProfileNotFoundException(
                        Exceptions.PROFILE.NOT_FOUND));


        // if the logged user is not admin
        if (!AuthHelper.isAdmin(currentCustomer)) {
            // we make sure that this profile belongs to the customer logged
            ProfileAuthorizationHelper
                    .authorize(currentCustomer, profile)
                    .checkOwner();

            // we validate the password before updating the profile
            AuthHelper.validatePassword(currentCustomer, request.currentPassword());
        }

        // we iterate over the fields (if any)
        request.fieldsToUpdate().forEach((key, value) -> {
            switch (key) {
                case "firstName" -> profile.setFirstName((String) value);
                case "lastName" -> profile.setLastName((String) value);
                case "phone" -> profile.setPhone((String) value);
                case "avatarFilename" -> profile.setImageFilename((String) value);
                case "gender" -> profile.setGender(CustomerGender.valueOf((String) value));
                case "birthdate" -> profile.setBirthdate(LocalDate.parse((String) value));
                default -> throw new ProfileUpdateValidationException(
                        Exceptions.PROFILE.INVALID_FIELD
                );
            }
        });

        // we change the updateAt timestamp field
        profile.setUpdatedAt(Instant.now());

        // we save the updated profile to the database
        return profileRepository.save(profile);
    }

    /**
     * Check if the username given exists
     *
     * @param username the username to check
     * @throws ProfileUpdateValidationException if the username is
     */
    public void usernameExists(String username) {
        profileRepository
                .findByUsername(username)
                .orElseThrow(
                        () -> new ProfileNotFoundException(Exceptions.PROFILE.NOT_FOUND)
                );
    }
}
