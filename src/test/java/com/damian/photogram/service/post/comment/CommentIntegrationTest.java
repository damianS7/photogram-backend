package com.damian.photogram.service.post.comment;

import com.damian.photogram.core.AbstractIntegrationTest;
import com.damian.photogram.core.util.JsonHelper;
import com.damian.photogram.domain.post.model.Comment;
import com.damian.photogram.domain.post.model.Post;
import com.damian.photogram.domain.user.enums.AccountStatus;
import com.damian.photogram.domain.user.enums.CustomerGender;
import com.damian.photogram.domain.user.enums.UserRole;
import com.damian.photogram.domain.user.model.Customer;
import com.damian.photogram.web.rest.post.dto.request.CommentCreateRequest;
import com.damian.photogram.web.rest.post.dto.response.CommentDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CommentIntegrationTest extends AbstractIntegrationTest {

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
    @DisplayName("Should get post comments paginated")
    void shouldGetPostCommentsPaginated() throws Exception {
        // given
        loginWithCustomer(customer);

        Post post = Post.create(customer)
                        .setDescription("Hello world.");

        postRepository.save(post);

        Comment comment1 = Comment.create(customer, post)
                                  .setMessage("Hello this is my post!");

        commentRepository.save(comment1);

        // when
        MvcResult result = mockMvc
                .perform(
                        get("/api/v1/posts/{id}/comments", post.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.OK.value()))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        String json = result.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(json);
        JsonNode contentNode = root.get("content");

        CommentDto[] commentsDto = JsonHelper.fromJson(contentNode.toString(), CommentDto[].class);

        // then
        assertThat(commentsDto[0])
                .isNotNull()
                .extracting(
                        CommentDto::id,
                        CommentDto::postId,
                        CommentDto::username,
                        CommentDto::message,
                        CommentDto::createdAt
                )
                .containsExactly(
                        comment1.getId(),
                        comment1.getPost().getId(),
                        comment1.getAuthor().getProfile().getUsername(),
                        comment1.getMessage(),
                        comment1.getCreatedAt().toString()
                );

    }

    @Test
    @DisplayName("Should comment in post")
    void shouldAddComment() throws Exception {
        // given
        loginWithCustomer(customer);

        Post post = Post.create(customer)
                        .setDescription("Hello world.");

        postRepository.save(post);

        CommentCreateRequest request = new CommentCreateRequest("This is my first comment.");

        // when
        MvcResult result = mockMvc
                .perform(
                        post("/api/v1/posts/{id}/comment", post.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(JsonHelper.toJson(request)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.CREATED.value()))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        CommentDto commentDto = JsonHelper.fromJson(
                result.getResponse().getContentAsString(),
                CommentDto.class
        );

        // then
        assertThat(commentDto)
                .isNotNull()
                .extracting(
                        CommentDto::id,
                        CommentDto::postId,
                        CommentDto::username,
                        CommentDto::message,
                        CommentDto::createdAt
                )
                .containsExactly(
                        commentDto.id(),
                        post.getId(),
                        customer.getProfile().getUsername(),
                        request.comment(),
                        commentDto.createdAt()
                );
    }

    @Test
    @DisplayName("Should delete comment in post")
    void shouldDeleteComment() throws Exception {
        // given
        loginWithCustomer(customer);

        Post post = Post.create(customer)
                        .setDescription("Hello world.");

        postRepository.save(post);

        Comment comment = Comment.create(customer, post)
                                 .setMessage("Hello this is my post!");

        commentRepository.save(comment);

        // when
        mockMvc
                .perform(
                        delete("/api/v1/comments/{id}", comment.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.NO_CONTENT.value()));
    }


}