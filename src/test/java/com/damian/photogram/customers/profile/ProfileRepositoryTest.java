package com.damian.photogram.customers.profile;

import com.damian.photogram.customers.Customer;
import com.damian.photogram.customers.CustomerGender;
import com.damian.photogram.customers.CustomerRepository;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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
        profileRepository.deleteAll();

        customer = new Customer();
        customer.setEmail("david@gmail.com");
        customer.setPassword("123456");
        customer.getProfile().setFirstName("david");
        customer.getProfile().setLastName("white");
        customer.getProfile().setPhone("123 123 123");
        customer.getProfile().setGender(CustomerGender.MALE);
        customer.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));
        customer.getProfile().setImageFilename("/images/photoPath.jpg");

        customerRepository.save(customer);
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
