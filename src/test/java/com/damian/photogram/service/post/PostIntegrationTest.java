package com.damian.photogram.service.post;

import com.damian.photogram.core.AbstractIntegrationTest;
import com.damian.photogram.core.util.ImageTestHelper;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.user.enums.AccountStatus;
import com.damian.photogram.domain.user.enums.CustomerGender;
import com.damian.photogram.domain.user.enums.UserRole;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.web.rest.post.dto.request.PostCreateRequest;
import com.damian.photogram.web.rest.post.dto.response.ImageUploadedDto;
import com.damian.photogram.web.rest.post.dto.response.PostDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PostIntegrationTest extends AbstractIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(PostIntegrationTest.class);
    private Customer customer;

    @BeforeAll
    void setUp() {
        customer = Customer.create()
                           .setEmail("customer@test.com")
                           .setPassword(passwordEncoder.encode(this.RAW_PASSWORD))
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
    @DisplayName("Should get posts paginated")
    void shouldGetPostsByUsernamePaginated() throws Exception {
        // given
        loginWithCustomer(customer);

        Post post = new Post(customer);
        post.setImageFilename("demo.jpg");
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
                        PostDto::imageFilename,
                        PostDto::createdAt
                )
                .containsExactly(
                        postsDto[0].id(),
                        postsDto[0].authorId(),
                        postsDto[0].description(),
                        postsDto[0].imageFilename(),
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
                        PostDto::imageFilename,
                        PostDto::createdAt
                )
                .containsExactly(
                        postDto.id(),
                        postDto.authorId(),
                        postDto.description(),
                        postDto.imageFilename(),
                        postDto.createdAt()

                );
    }

    @Test
    @DisplayName("Should delete a post")
    void shouldDeletePost() throws Exception {
        // given
        loginWithCustomer(customer);

        Post post = new Post(customer);
        post.setImageFilename("image.jpg");
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

    @Test
    @DisplayName("Should upload post image")
    void shouldUploadPostImage() throws Exception {
        // given
        loginWithCustomer(customer);
        MockMultipartFile givenImage = ImageTestHelper.createDefaultJpg();

        // when
        MvcResult result = mockMvc
                .perform(
                        multipart("/api/v1/posts/image")
                                .file(givenImage)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .param("currentPassword", this.RAW_PASSWORD)
                                .with(request -> {
                                    request.setMethod("POST");
                                    return request;
                                }))

                .andDo(print())
                .andExpect(status().is(201))
                .andReturn();

        // then
        ImageUploadedDto imageUploadedDto = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ImageUploadedDto.class
        );

        // then
        assertThat(imageUploadedDto)
                .isNotNull()
                .extracting(ImageUploadedDto::imageFilename)
                .isEqualTo(imageUploadedDto.imageFilename());
    }

    @Test
    @DisplayName("Should not upload post image when image is empty")
    void shouldNotUploadPostImageWhenNotImageIsEmpty() throws Exception {
        // given
        loginWithCustomer(customer);

        MockMultipartFile givenImage = new MockMultipartFile(
                "file",
                "photo.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        // when
        MvcResult result = mockMvc
                .perform(
                        multipart("/api/v1/posts/image")
                                .file(givenImage)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .param("currentPassword", this.RAW_PASSWORD)
                                .with(request -> {
                                    request.setMethod("POST");
                                    return request;
                                }))

                .andDo(print())
                .andExpect(status().is(400))
                .andReturn();
    }

}