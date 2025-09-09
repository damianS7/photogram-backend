package com.damian.photogram.domain.customer;

import com.damian.photogram.AbstractIntegrationTest;
import com.damian.photogram.app.user.UserRole;
import com.damian.photogram.domain.account.enums.AccountStatus;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.model.Customer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDate;

// TODO review this
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProfileImageIntegrationTest extends AbstractIntegrationTest {

    private Customer customerA;
    private Customer customerB;
    private Customer customerAdmin;

    @BeforeAll
    void setUp() throws Exception {
        customerA = Customer.create()
                            .setEmail("customerA@test.com")
                            .setPassword(bCryptPasswordEncoder.encode(this.RAW_PASSWORD))
                            .setRole(UserRole.CUSTOMER)
                            .setProfile(profile -> profile
                                    .setFirstName("John")
                                    .setLastName("Wick")
                                    .setGender(CustomerGender.MALE)
                                    .setBirthdate(LocalDate.of(1989, 1, 1))
                                    .setImageFilename("avatar.jpg")
                            );
        customerA.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customerRepository.save(customerA);

        customerB = Customer.create()
                            .setEmail("customerB@test.com")
                            .setPassword(bCryptPasswordEncoder.encode(this.RAW_PASSWORD)
                            );
        customerB.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customerRepository.save(customerB);

        customerAdmin = Customer.create()
                                .setEmail("admin@test.com")
                                .setRole(UserRole.ADMIN)
                                .setPassword(bCryptPasswordEncoder.encode(this.RAW_PASSWORD)
                                );
        customerAdmin.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customerRepository.save(customerAdmin);
    }

}