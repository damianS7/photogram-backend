package com.damian.photogram.domain.post;

import com.damian.photogram.app.auth.dto.AuthenticationRequest;
import com.damian.photogram.app.auth.dto.AuthenticationResponse;
import com.damian.photogram.domain.account.enums.AccountStatus;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.enums.CustomerRole;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.customer.repository.CustomerRepository;
import com.damian.photogram.domain.post.dto.response.LikeDto;
import com.damian.photogram.domain.post.dto.response.PostLikeDataDto;
import com.damian.photogram.domain.post.model.Like;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.post.repository.LikeRepository;
import com.damian.photogram.domain.post.repository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LikeIntegrationTest {
    private final String RAW_PASSWORD = "123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private Customer customer;
    private String token;

    @AfterAll
    void tearDown() {
        likeRepository.deleteAll();
        postRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @BeforeAll
    void setUp() {
        customer = Customer.create()
                           .setMail("customer@test.com")
                           .setPassword(bCryptPasswordEncoder.encode(this.RAW_PASSWORD))
                           .setRole(CustomerRole.CUSTOMER)
                           .setProfile(profile -> profile
                                   .setFirstName("John")
                                   .setLastName("Wick")
                                   .setUsername("John")
                                   .setGender(CustomerGender.MALE)
                                   .setBirthdate(LocalDate.of(1989, 1, 1))
                                   .setImageFilename("avatar.jpg")
                           );
        customer.getAccount().setAccountStatus(AccountStatus.VERIFIED);
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
    @DisplayName("Should get post like data")
    void shouldGetPostLikeData() throws Exception {
        // given
        loginWithCustomer(customer);


        Post post = new Post(customer);
        post.setDescription("Hello world.");
        postRepository.save(post);

        Like like = new Like(post, customer);
        likeRepository.save(like);

        // when
        MvcResult result = mockMvc
                .perform(
                        get("/api/v1/posts/{id}/likes", post.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        PostLikeDataDto postLikeData = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                PostLikeDataDto.class
        );

        // then
        assertThat(postLikeData)
                .isNotNull()
                .extracting(
                        PostLikeDataDto::postId,
                        PostLikeDataDto::hasBeenLiked,
                        PostLikeDataDto::totalLikes
                ).containsExactly(post.getId(), true, 1L);

    }

    @Test
    @DisplayName("Should like a post")
    void shouldLikePost() throws Exception {
        // given
        loginWithCustomer(customer);


        Post post = new Post(customer);
        postRepository.save(post);

        // when
        MvcResult result = mockMvc
                .perform(
                        post("/api/v1/posts/{id}/like", post.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(201))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        LikeDto likeDto = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LikeDto.class
        );

        // then
        assertThat(likeDto)
                .isNotNull()
                .extracting(
                        LikeDto::postId,
                        LikeDto::customerId
                ).containsExactly(post.getId(), customer.getId());

    }

    @Test
    @DisplayName("Should unlike a post")
    void shouldUnlikePost() throws Exception {
        // given
        loginWithCustomer(customer);


        Post post = new Post(customer);
        postRepository.save(post);

        Like like = new Like(post, customer);
        likeRepository.save(like);

        // when
        mockMvc
                .perform(
                        delete("/api/v1/posts/{id}/unlike", post.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(204))
                .andReturn();
    }
}