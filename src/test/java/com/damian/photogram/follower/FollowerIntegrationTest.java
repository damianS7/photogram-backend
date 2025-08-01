package com.damian.photogram.follower;

import com.damian.photogram.auth.http.AuthenticationRequest;
import com.damian.photogram.auth.http.AuthenticationResponse;
import com.damian.photogram.customer.Customer;
import com.damian.photogram.customer.CustomerGender;
import com.damian.photogram.customer.CustomerRepository;
import com.damian.photogram.customer.CustomerRole;
import com.damian.photogram.follower.dto.FollowerDTO;
import com.damian.photogram.follower.http.FriendCreateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
public class FollowerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private FollowerRepository followerRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private Customer customer;
    private String token;

    @BeforeEach
    void setUp() {
        followerRepository.deleteAll();
        customerRepository.deleteAll();

        customer = new Customer();
        customer.setRole(CustomerRole.CUSTOMER);
        customer.setEmail("customer@test.com");
        customer.setPassword(bCryptPasswordEncoder.encode("123456"));

        customer.getProfile().setFirstName("John");
        customer.getProfile().setLastName("Wick");
        customer.getProfile().setGender(CustomerGender.MALE);
        customer.getProfile().setBirthdate(LocalDate.of(1989, 1, 1));

        customerRepository.save(customer);
    }

    void loginWithCustomer(Customer customer) throws Exception {
        // given
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                customer.getEmail(), "123456"
        );

        String jsonRequest = objectMapper.writeValueAsString(authenticationRequest);

        // when
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                                          .contentType(MediaType.APPLICATION_JSON)
                                          .content(jsonRequest))
                                  .andReturn();

        AuthenticationResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthenticationResponse.class
        );

        token = response.token();
    }

    @Test
    @DisplayName("Should get friends")
    void shouldGetFriends() throws Exception {
        // given
        loginWithCustomer(customer);

        Customer customerFriend = new Customer(
                "follower@test.com",
                bCryptPasswordEncoder.encode("123456")
        );
        customerRepository.save(customerFriend);
        followerRepository.save(
                new Follower(customer, customerFriend)
        );

        // when
        MvcResult result = mockMvc
                .perform(
                        get("/api/v1/friends")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        FollowerDTO[] followerDTO = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                FollowerDTO[].class
        );

        // then
        assertThat(followerDTO).isNotNull();
        assertThat(followerDTO.length).isGreaterThanOrEqualTo(1);
        assertThat(followerDTO[0].customerId()).isEqualTo(customerFriend.getId());
    }

    @Test
    @DisplayName("Should add a follower")
    void shouldAddFriend() throws Exception {
        // given
        loginWithCustomer(customer);

        Customer friend = new Customer(
                "follower@test.com",
                bCryptPasswordEncoder.encode("123456")
        );
        customerRepository.save(friend);

        FriendCreateRequest friendCreateRequest = new FriendCreateRequest(
                friend.getId()
        );

        // when
        MvcResult result = mockMvc
                .perform(
                        post("/api/v1/friends")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(friendCreateRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(201))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        FollowerDTO followerDTO = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                FollowerDTO.class
        );

        // then
        assertThat(followerDTO).isNotNull();
        assertEquals(followerDTO.customerId(), friend.getId());
    }

    @Test
    @DisplayName("Should not add a follower when already exists")
    void shouldNotAddFriendWhenAlreadyExists() throws Exception {
        // given
        loginWithCustomer(customer);

        Customer customerFriend = new Customer(
                "follower@test.com",
                bCryptPasswordEncoder.encode("123456")
        );
        customerRepository.save(customerFriend);

        Follower follower = new Follower(customer, customerFriend);
        followerRepository.save(follower);

        FriendCreateRequest friendCreateRequest = new FriendCreateRequest(
                customerFriend.getId()
        );

        // when
        mockMvc
                .perform(
                        post("/api/v1/friends")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(friendCreateRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(409))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    @Test
    @DisplayName("Should not add a follower when customer not found")
    void shouldNotAddFriendWhenCustomerNotFount() throws Exception {
        // given
        loginWithCustomer(customer);

        Customer customerFriend = new Customer(
                "follower@test.com",
                bCryptPasswordEncoder.encode("123456")
        );
        customerRepository.save(customerFriend);

        FriendCreateRequest friendCreateRequest = new FriendCreateRequest(
                5L
        );

        // when
        mockMvc
                .perform(
                        post("/api/v1/friends")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(friendCreateRequest)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(404))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    @Test
    @DisplayName("Should delete a follower")
    void shouldDeleteFriend() throws Exception {
        // given
        loginWithCustomer(customer);

        Customer customerFriend = new Customer(
                "follower@test.com",
                bCryptPasswordEncoder.encode("123456")
        );
        customerRepository.save(customerFriend);

        Follower givenFollower = new Follower(customer, customerFriend);
        followerRepository.save(givenFollower);

        // when
        MvcResult result = mockMvc
                .perform(
                        delete("/api/v1/friends/{id}", givenFollower.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(204))
                .andReturn();

        // then
    }

    @Test
    @DisplayName("Should not delete follower when not found")
    void shouldNotDeleteFriendWhenNotFound() throws Exception {
        // given
        loginWithCustomer(customer);

        Customer customerFriend = new Customer(
                "follower@test.com",
                bCryptPasswordEncoder.encode("123456")
        );
        customerRepository.save(customerFriend);

        // when
        mockMvc
                .perform(
                        delete("/api/v1/friends/{id}", 25L)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(404))
                .andReturn();

        // then
    }

    @Test
    @DisplayName("Should not delete follower when not your follower")
    void shouldNotDeleteFriendWhenNotYourFriend() throws Exception {
        // given
        loginWithCustomer(customer);

        Customer customerA = new Customer(
                "customerA@test.com",
                bCryptPasswordEncoder.encode("123456")
        );
        customerRepository.save(customerA);

        Customer customerFriend = new Customer(
                "follower@test.com",
                bCryptPasswordEncoder.encode("123456")
        );
        customerRepository.save(customerFriend);

        Follower givenFollower = new Follower(customerA, customerFriend);
        followerRepository.save(givenFollower);

        // when
        mockMvc
                .perform(
                        delete("/api/v1/friends/{id}", givenFollower.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(403))
                .andReturn();

        // then
    }


}
