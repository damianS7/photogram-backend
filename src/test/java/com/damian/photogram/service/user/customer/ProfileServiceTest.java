package com.damian.photogram.service.user.customer;

import com.damian.photogram.core.AbstractServiceTest;
import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.domain.user.enums.CustomerGender;
import com.damian.photogram.domain.user.exception.AccountInvalidPasswordConfirmationException;
import com.damian.photogram.domain.user.exception.ProfileNotFoundException;
import com.damian.photogram.domain.user.exception.ProfileNotOwnerException;
import com.damian.photogram.domain.user.exception.ProfileUpdateException;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.domain.user.model.Profile;
import com.damian.photogram.domain.user.repository.ProfileRepository;
import com.damian.photogram.service.user.ProfileService;
import com.damian.photogram.web.rest.user.dto.request.ProfileUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProfileServiceTest extends AbstractServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ProfileService profileService;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = Customer.create()
                           .setId(2L)
                           .setEmail("customer@test.com")
                           .setPassword(passwordEncoder.encode(RAW_PASSWORD))
                           .setProfile(profile -> profile
                                   .setId(5L)
                                   .setFirstName("John")
                                   .setLastName("Wick")
                                   .setGender(CustomerGender.MALE)
                                   .setBirthdate(LocalDate.of(1989, 1, 1))
                           );
    }

    @Test
    @DisplayName("Should update profile")
    void shouldUpdateProfile() {
        // given
        setUpContext(customer);

        Map<String, Object> fields = new HashMap<>();
        fields.put("firstName", "David");
        fields.put("lastName", "David");
        fields.put("birthdate", "1904-01-02");
        fields.put("gender", "MALE");
        fields.put("phone", "9199191919");
        fields.put("avatarFilename", "image.jpg");
        ProfileUpdateRequest givenRequest = new ProfileUpdateRequest(
                RAW_PASSWORD,
                fields
        );

        // when
        when(profileRepository.findById(customer.getProfile().getId())).thenReturn(Optional.of(customer.getProfile()));
        when(profileRepository.save(any(Profile.class))).thenReturn(customer.getProfile());

        Profile result = profileService.updateProfile(givenRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo(givenRequest.fieldsToUpdate().get("firstName"));
        assertThat(result.getLastName()).isEqualTo(givenRequest.fieldsToUpdate().get("lastName"));
        assertThat(result.getPhone()).isEqualTo(givenRequest.fieldsToUpdate().get("phone"));
        assertThat(result.getBirthdate().toString()).isEqualTo(givenRequest.fieldsToUpdate().get("birthdate"));
        assertThat(result.getGender().toString()).isEqualTo(givenRequest.fieldsToUpdate().get("gender"));
        verify(profileRepository, times(1)).save(customer.getProfile());
    }

    @Test
    @DisplayName("Should not update profile when password is wrong")
    void shouldNotUpdateProfileWhenPasswordIsWrong() {
        // given
        setUpContext(customer);

        Map<String, Object> fields = new HashMap<>();
        fields.put("firstName", "David");
        ProfileUpdateRequest givenRequest = new ProfileUpdateRequest(
                "wrongPassword1",
                fields
        );

        // when
        when(profileRepository.findById(customer.getProfile().getId())).thenReturn(Optional.of(customer.getProfile()));
        AccountInvalidPasswordConfirmationException exception = assertThrows(
                AccountInvalidPasswordConfirmationException.class,
                () -> profileService.updateProfile(givenRequest)
        );

        // Then
        assertEquals(Exceptions.ACCOUNT.INVALID_PASSWORD, exception.getMessage());
    }

    @Test
    @DisplayName("Should not update profile when profile not found")
    void shouldNotUpdateProfileWhenProfileNotFound() {
        // given
        setUpContext(customer);

        Map<String, Object> fields = new HashMap<>();
        fields.put("firstName", "David");
        ProfileUpdateRequest givenRequest = new ProfileUpdateRequest(
                RAW_PASSWORD,
                fields
        );

        // when
        when(profileRepository.findById(customer.getProfile().getId())).thenReturn(Optional.empty());
        ProfileNotFoundException exception = assertThrows(
                ProfileNotFoundException.class,
                () -> profileService.updateProfile(givenRequest)
        );

        // Then
        assertEquals(Exceptions.CUSTOMER.PROFILE.NOT_FOUND, exception.getMessage());
    }

    @Test
    @DisplayName("Should not update profile when profile not found")
    void shouldNotUpdateProfileWhenProfileNotYours() {
        // given
        setUpContext(customer);

        Map<String, Object> fields = new HashMap<>();
        fields.put("firstName", "David");
        ProfileUpdateRequest givenRequest = new ProfileUpdateRequest(
                RAW_PASSWORD,
                fields
        );

        Profile givenProfile = new Profile();
        givenProfile.setOwner(new Customer(5L, "customer@test.com", "12345"));

        // when
        when(profileRepository.findById(customer.getProfile().getId())).thenReturn(Optional.of(givenProfile));
        ProfileNotOwnerException exception = assertThrows(
                ProfileNotOwnerException.class,
                () -> profileService.updateProfile(givenRequest)
        );

        // Then
        assertEquals(Exceptions.CUSTOMER.PROFILE.NOT_OWNER, exception.getMessage());
    }

    @Test
    @DisplayName("Should not update profile when profile not found")
    void shouldNotUpdateProfileWhenInvalidField() {
        // given
        setUpContext(customer);

        Map<String, Object> fields = new HashMap<>();
        fields.put("firstName", "David");
        fields.put("fakeField", "1234");
        ProfileUpdateRequest givenRequest = new ProfileUpdateRequest(
                RAW_PASSWORD,
                fields
        );

        // when
        when(profileRepository.findById(customer.getProfile().getId())).thenReturn(Optional.of(customer.getProfile()));
        ProfileUpdateException exception = assertThrows(
                ProfileUpdateException.class,
                () -> profileService.updateProfile(givenRequest)
        );

        // Then
        assertEquals(Exceptions.CUSTOMER.PROFILE.UPDATE_FAILED_INVALID_FIELD, exception.getMessage());
    }
}
