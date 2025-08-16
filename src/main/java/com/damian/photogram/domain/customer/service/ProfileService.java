package com.damian.photogram.domain.customer.service;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.utils.AuthHelper;
import com.damian.photogram.domain.customer.dto.request.ProfileUpdateRequest;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.exception.ProfileAuthorizationException;
import com.damian.photogram.domain.customer.exception.ProfileNotFoundException;
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
     * returns a profile
     *
     * @param profileId the profile id
     * @return Profile the profile
     */
    public Profile getProfile(Long profileId) {
        return profileRepository
                .findById(profileId)
                .orElseThrow(
                        () -> new ProfileNotFoundException(Exceptions.PROFILE.NOT_FOUND)
                );
    }

    // it updates the logged customer profile
    public Profile updateProfile(ProfileUpdateRequest request) {
        final Customer customerLogged = AuthHelper.getLoggedCustomer();

        return this.updateProfile(customerLogged.getProfile().getId(), request);
    }

    // it updates a profile by id
    public Profile updateProfile(Long profileId, ProfileUpdateRequest request) {
        // We get the profile we want to modify
        Profile profile = profileRepository
                .findById(profileId)
                .orElseThrow(() -> new ProfileNotFoundException(
                        Exceptions.PROFILE.NOT_FOUND));

        final Customer customerLogged = AuthHelper.getLoggedCustomer();

        // if the logged user is not admin
        if (!AuthHelper.isAdmin(customerLogged)) {
            // we make sure that this profile belongs to the customer logged
            ProfileAuthorizationHelper
                    .authorize(customerLogged, profile)
                    .checkOwner();

            AuthHelper.validatePassword(customerLogged, request.currentPassword());
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
                default -> throw new ProfileAuthorizationException(
                        Exceptions.PROFILE.INVALID_FIELD
                );
            }
        });

        // we change the updateAt timestamp field
        profile.setUpdatedAt(Instant.now());

        return profileRepository.save(profile);
    }

    // check if the username given exists
    public void checkUsername(String username) {
        profileRepository
                .findByUsername(username)
                .orElseThrow(
                        () -> new ProfileNotFoundException(Exceptions.PROFILE.NOT_FOUND)
                );
    }
}
