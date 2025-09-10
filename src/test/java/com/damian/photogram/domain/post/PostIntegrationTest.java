package com.damian.photogram.domain.post;

import com.damian.photogram.AbstractIntegrationTest;
import com.damian.photogram.domain.customer.enums.UserRole;
import com.damian.photogram.domain.account.enums.AccountStatus;
import com.damian.photogram.domain.customer.enums.CustomerGender;
import com.damian.photogram.domain.customer.model.Customer;
import com.damian.photogram.domain.post.dto.request.PostCreateRequest;
import com.damian.photogram.domain.post.dto.response.PostDto;
import com.damian.photogram.domain.post.model.Post;
import com.fasterxml.jackson.databind.JsonNode;
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
public class PostIntegrationTest extends AbstractIntegrationTest {

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
                                   .setImageFilename("avatar.jpg")
                           );
        customer.getAccount().setAccountStatus(AccountStatus.VERIFIED);
        customerRepository.save(customer);
    }

    @Test
    @DisplayName("Should get posts paginated")
    void shouldGetPostsByUsernamePaginated() throws Exception {
        // given
        loginWithCustomer(customer);

        Post post = new Post(customer);
        post.setPhotoFilename("demo.jpg");
        post.setDescription("Hello world.");
        postRepository.save(post);

        // when
        MvcResult result = mockMvc
                .perform(
                        get("/api/v1/posts/{id}", customer.getProfile().getUsername())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        String json = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        JsonNode contentNode = root.get("content");

        PostDto[] postsDto = objectMapper.treeToValue(contentNode, PostDto[].class);

        // then
        assertThat(postsDto[0])
                .isNotNull()
                .extracting(
                        PostDto::id,
                        PostDto::authorId,
                        PostDto::description,
                        PostDto::photoFilename,
                        PostDto::createdAt
                )
                .containsExactly(
                        postsDto[0].id(),
                        postsDto[0].authorId(),
                        postsDto[0].description(),
                        postsDto[0].photoFilename(),
                        postsDto[0].createdAt()

                );

    }

    @Test
    @DisplayName("Should create post")
    void shouldCreatePost() throws Exception {
        // given
        loginWithCustomer(customer);

        PostCreateRequest request = new PostCreateRequest(
                "photo.jpg",
                "hello world!"
        );

        // when
        MvcResult result = mockMvc
                .perform(
                        post("/api/v1/posts")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(request)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        PostDto postDto = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                PostDto.class
        );

        // then
        assertThat(postDto)
                .isNotNull()
                .extracting(
                        PostDto::id,
                        PostDto::authorId,
                        PostDto::description,
                        PostDto::photoFilename,
                        PostDto::createdAt
                )
                .containsExactly(
                        postDto.id(),
                        postDto.authorId(),
                        postDto.description(),
                        postDto.photoFilename(),
                        postDto.createdAt()

                );
    }

    @Test
    @DisplayName("Should delete a post")
    void shouldDeletePost() throws Exception {
        // given
        loginWithCustomer(customer);

        Post post = new Post(customer);
        post.setDescription("Hello world.");
        postRepository.save(post);

        // when
        mockMvc
                .perform(
                        delete("/api/v1/posts/{id}", post.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(204))
                .andReturn();
    }
}