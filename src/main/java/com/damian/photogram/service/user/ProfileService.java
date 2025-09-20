package com.damian.photogram.service.user;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.util.AuthHelper;
import com.damian.photogram.domain.user.enums.CustomerGender;
import com.damian.photogram.domain.user.exception.ProfileNotFoundException;
import com.damian.photogram.domain.user.exception.ProfileNotOwnerException;
import com.damian.photogram.domain.user.exception.ProfileUpdateException;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.domain.user.model.Profile;
import com.damian.photogram.domain.user.repository.ProfileRepository;
import com.damian.photogram.web.rest.user.dto.request.ProfileUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;

@Service
public class ProfileService {
    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);
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
        log.debug("Getting profile: {}", profileId);

        return profileRepository
                .findById(profileId)
                .orElseThrow(
                        () -> new ProfileNotFoundException(Exceptions.CUSTOMER.PROFILE.NOT_FOUND, profileId, null)
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
        log.debug("customer: {} updating profile: {}", currentCustomer.getId(), profileId);

        // find the profile we want to modify
        Profile profile = profileRepository
                .findById(profileId)
                .orElseThrow(() -> new ProfileNotFoundException(
                        Exceptions.CUSTOMER.PROFILE.NOT_FOUND, profileId, currentCustomer.getId())
                );


        // if the logged user is not admin
        if (!AuthHelper.isAdmin(currentCustomer)) {
            // we make sure that this profile belongs to the customer logged
            if (!profile.belongsTo(currentCustomer)) {
                throw new ProfileNotOwnerException(
                        Exceptions.CUSTOMER.PROFILE.NOT_OWNER,
                        profileId,
                        currentCustomer.getId()
                );
            }

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
                default -> throw new ProfileUpdateException(
                        Exceptions.CUSTOMER.PROFILE.UPDATE_FAILED_INVALID_FIELD, profileId, currentCustomer.getId()
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
     * @throws ProfileNotFoundException if the username is not found
     */
    public void userProfileExists(String username) {
        Customer currentCustomer = AuthHelper.getLoggedCustomer();
        log.debug("Customer: {} checking if username: {} exists", currentCustomer.getId(), username);

        if (!profileRepository.existsByUsernameIgnoreCase(username)) {
            log.warn("Failed to find a profile with username: {}", username);
            throw new ProfileNotFoundException(Exceptions.CUSTOMER.PROFILE.NOT_FOUND, null, null);
        }
    }
}
