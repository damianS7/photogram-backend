package com.damian.photogram.service.user.customer;

import com.damian.photogram.core.AbstractRepositoryTest;
import com.damian.photogram.domain.user.enums.CustomerGender;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.domain.user.model.Profile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProfileRepositoryTest extends AbstractRepositoryTest {

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = Customer.create()
                           .setEmail("david@demo.com")
                           .setPassword("123456")
                           .setProfile(profile -> profile
                                   .setFirstName("John")
                                   .setLastName("Wick")
                                   .setGender(CustomerGender.MALE)
                                   .setBirthdate(LocalDate.of(1989, 1, 1))
                                   .setImageFilename("images/avatar.jpg")
                           );

        customerRepository.save(customer);
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
        profileRepository.deleteAll();
    }

    @Test
    void shouldFindProfile() {
        // given
        Long profileId = customer.getProfile().getId();

        // when
        Profile profile = profileRepository.findById(profileId).orElseThrow();

        // then
        assertThat(profile.getId()).isNotNull();
        assertThat(profile.getCustomerId()).isEqualTo(customer.getId());
        assertThat(profile.getFirstName()).isEqualTo(customer.getProfile().getFirstName());
    }

    @Test
    void shouldNotFindProfile() {
        // given
        Long profileId = -1L;

        // when
        boolean profileExists = profileRepository.existsById(profileId);

        // then
        assertThat(profileExists).isFalse();
    }

    @Test
    void shouldNotFindProfileWhenIdIsNull() {
        // given
        Long profileId = null;

        // when
        // then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> profileRepository.findById(profileId)
        );
    }

    @Test
    void shouldUpdateProfile() {
        // given
        Long profileId = customer.getProfile().getId();
        final String newName = "Ronald";

        // when
        customer.getProfile().setFirstName(newName);
        profileRepository.save(customer.getProfile());
        Profile profile = profileRepository.findById(profileId).orElseThrow();

        // then
        assertThat(profile.getFirstName()).isEqualTo(newName);
    }
}
