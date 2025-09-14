package com.damian.photogram.domain.post;

import com.damian.photogram.AbstractIntegrationTest;
import com.damian.photogram.domain.user.account.enums.AccountStatus;
import com.damian.photogram.domain.user.customer.enums.CustomerGender;
import com.damian.photogram.domain.user.customer.enums.UserRole;
import com.damian.photogram.domain.user.customer.model.Customer;
import com.damian.photogram.domain.post.dto.response.LikeDto;
import com.damian.photogram.domain.post.dto.response.PostLikeDataDto;
import com.damian.photogram.domain.post.model.Like;
import com.damian.photogram.domain.post.model.Post;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LikeIntegrationTest extends AbstractIntegrationTest {

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
                                   .setUsername("John")
                                   .setGender(CustomerGender.MALE)
                                   .setBirthdate(LocalDate.of(1989, 1, 1))
                                   .setImageFilename("images/avatar.jpg")
                           );
        customer.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customerRepository.save(customer);
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