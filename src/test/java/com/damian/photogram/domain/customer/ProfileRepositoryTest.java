package com.damian.photogram.domain.customer;

import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.model.Profile;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import com.damian.photogram.domain.customer.repository.ProfileRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
public class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Faker faker;
    private Customer customer;

    @BeforeEach
    void setUp() {
        faker = new Faker();

        customer = Customer.create()
                           .setMail("david@demo.com")
                           .setPassword("123456")
                           .setProfile(profile -> profile
                                   .setFirstName("John")
                                   .setLastName("Wick")
                                   .setGender(CustomerGender.MALE)
                                   .setBirthdate(LocalDate.of(1989, 1, 1))
                                   .setImageFilename("avatar.jpg")
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
