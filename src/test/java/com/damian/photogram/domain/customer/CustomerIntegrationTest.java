package com.damian.photogram.domain.customer;

import com.damian.photogram.AbstractIntegrationTest;
import com.damian.photogram.app.user.UserRole;
import com.damian.photogram.domain.account.enums.AccountStatus;
import com.damian.photogram.domain.customer.dto.request.CustomerEmailUpdateRequest;
import com.damian.photogram.domain.customer.dto.response.CustomerDto;
import com.damian.photogram.domain.customer.dto.response.CustomerWithProfileDto;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.model.Customer;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CustomerIntegrationTest extends AbstractIntegrationTest {

    private Customer customer;

    @BeforeAll
    void setUp() {
        customer = Customer.create()
                           .setEmail("customer@test.com")
                           .setPassword(bCryptPasswordEncoder.encode(this.RAW_PASSWORD))
                           .setRole(UserRole.CUSTOMER)
                           .setProfile(profile -> profile
                                   .setFirstName("John")
                                   .setLastName("Wick")
                                   .setGender(CustomerGender.MALE)
                                   .setBirthdate(LocalDate.of(1989, 1, 1))
                                   .setImageFilename("avatar.jpg")
                           );
        customer.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customerRepository.save(customer);
    }

    @AfterAll
    void tearDown() {
        customerRepository.deleteAll();
    }

    @Test
    @DisplayName("Should get logged customer")
    void shouldGetCustomer() throws Exception {
        // given
        loginWithCustomer(customer);

        // when
        MvcResult result = mockMvc
                .perform(
                        get("/api/v1/customers")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        CustomerWithProfileDto customerWithProfileDTO = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CustomerWithProfileDto.class
        );

        // then
        assertThat(customerWithProfileDTO).isNotNull();
        assertThat(customerWithProfileDTO.email()).isEqualTo(customer.getEmail());
    }

    @Test
    @DisplayName("Should update email")
    void shouldUpdateEmail() throws Exception {
        // given
        loginWithCustomer(customer);

        CustomerEmailUpdateRequest givenRequest = new CustomerEmailUpdateRequest(
                RAW_PASSWORD,
                "customer2@test.com"
        );

        // when
        MvcResult result = mockMvc
                .perform(
                        patch("/api/v1/customers/email")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(givenRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        // then
        CustomerDto customerDTO = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                CustomerDto.class
        );

        // then
        assertThat(customerDTO).isNotNull();
        assertThat(customerDTO.email()).isEqualTo(givenRequest.newEmail());
    }
}
