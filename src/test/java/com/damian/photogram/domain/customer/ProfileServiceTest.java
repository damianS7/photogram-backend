package com.damian.photogram.domain.customer;

import com.damian.photogram.core.exception.Exceptions;
import com.damian.photogram.core.exception.PasswordMismatchException;
import com.damian.photogram.domain.customer.dto.request.ProfileUpdateRequest;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.exception.ProfileAuthorizationException;
import com.damian.photogram.domain.customer.exception.ProfileNotFoundException;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.model.Profile;
import com.damian.photogram.domain.customer.repository.ProfileRepository;
import com.damian.photogram.domain.customer.service.ProfileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

    private final String RAW_PASSWORD = "123456";

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private ProfileService profileService;
    private Customer customer;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        profileRepository.deleteAll();

        customer = new Customer();
        customer.setId(2L);
        customer.setEmail("customer@test.com");
        customer.setPassword(passwordEncoder.encode(RAW_PASSWORD));
        customer.getProfile().setId(5L);
        customer.getProfile().setFirstName("John");
        customer.getProfile().setLastName("Wick");
        customer.getProfile().setGender(CustomerGender.MALE);
        customer.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    void setUpContext(Customer customer) {
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(customer);
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
        PasswordMismatchException exception = assertThrows(
                PasswordMismatchException.class,
                () -> profileService.updateProfile(givenRequest)
        );

        // Then
        assertEquals(PasswordMismatchException.PASSWORD_MISMATCH, exception.getMessage());
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
        assertEquals(Exceptions.PROFILE.NOT_FOUND, exception.getMessage());
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
        givenProfile.setCustomer(new Customer(5L, "customer@test.com", "12345"));

        // when
        when(profileRepository.findById(customer.getProfile().getId())).thenReturn(Optional.of(givenProfile));
        ProfileAuthorizationException exception = assertThrows(
                ProfileAuthorizationException.class,
                () -> profileService.updateProfile(givenRequest)
        );

        // Then
        assertEquals(Exceptions.PROFILE.ACCESS_FORBIDDEN, exception.getMessage());
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
        ProfileAuthorizationException exception = assertThrows(
                ProfileAuthorizationException.class,
                () -> profileService.updateProfile(givenRequest)
        );

        // Then
        assertEquals(Exceptions.PROFILE.INVALID_FIELD, exception.getMessage());
    }
}
